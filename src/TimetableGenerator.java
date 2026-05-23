import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates an optimised {@link Timetable} for a student given their selected topics,
 * available class instances, and ordering preferences.
 *
 * <h2>Algorithm overview</h2>
 * <ol>
 *   <li>Group available {@link ClassInstance} objects by topic code.</li>
 *   <li>Score every instance against the student's {@link UserPreference} list.</li>
 *   <li>Within each topic group, sort instances by descending preference score so
 *       higher-priority options are tried first.</li>
 *   <li>Recursively build combinations (one instance per topic) using backtracking,
 *       pruning any branch where a new instance conflicts with already-selected ones.</li>
 *   <li>Among all valid full combinations, pick the one with the highest total score.</li>
 * </ol>
 *
 * <p>Preference types recognised (case-insensitive):
 * <ul>
 *   <li>{@code PREFER_CAMPUS:<name>}   — favour instances on the named campus</li>
 *   <li>{@code AVOID_CAMPUS:<name>}    — penalise instances on the named campus</li>
 *   <li>{@code PREFER_FORMAT:<format>} — favour instances with the named class format</li>
 *   <li>{@code AVOID_FORMAT:<format>}  — penalise instances with the named class format</li>
 *   <li>{@code MINIMIZE_GAPS}          — favour instances whose sessions have fewer idle gaps</li>
 *   <li>{@code PREFER_MORNING}         — favour instances whose sessions start before noon</li>
 *   <li>{@code PREFER_AFTERNOON}       — favour instances whose sessions start at noon or after</li>
 * </ul>
 */
public class TimetableGenerator {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final double PREFERENCE_SCORE_STEP = 100.0;
    private static final double AVOID_PENALTY         = -150.0;
    private static final int    GAP_THRESHOLD_MINUTES = 60;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final ConflictDetector conflictDetector;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public TimetableGenerator() {
        this.conflictDetector = new ConflictDetector();
    }

    public TimetableGenerator(ConflictDetector conflictDetector) {
        this.conflictDetector = conflictDetector;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates the best valid timetable for the given topics and preferences.
     *
     * @param timetableName      display name for the resulting timetable
     * @param semester           semester string (e.g. "Semester 1 2025")
     * @param selectedTopics     topics the student wants to study (must not be empty)
     * @param availableInstances all class instances available for selection
     * @param preferences        student preferences used to rank instances (may be empty)
     * @param allowLectureOverlap whether lecture–lecture overlaps are permitted
     * @param selectedCampuses   campuses the student is willing to attend
     * @return the best {@link Timetable}, or {@code null} if no valid combination exists
     * @throws IllegalArgumentException if {@code selectedTopics} is null or empty
     */
    public Timetable generateTimetable(String timetableName,
                                       String semester,
                                       List<Topic> selectedTopics,
                                       List<ClassInstance> availableInstances,
                                       List<UserPreference> preferences,
                                       boolean allowLectureOverlap,
                                       List<String> selectedCampuses) {

        if (selectedTopics == null || selectedTopics.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one topic must be selected before generating a timetable.");
        }

        List<UserPreference> sortedPreferences = sortPreferences(preferences);
        List<String> normalisedCampuses        = normaliseCampuses(selectedCampuses);

        Map<String, List<ClassInstance>> instancesByTopic =
                groupInstancesByTopic(selectedTopics, availableInstances, normalisedCampuses);

        for (Topic topic : selectedTopics) {
            List<ClassInstance> candidates = instancesByTopic.get(topic.getTopicCode());
            if (candidates == null || candidates.isEmpty()) {
                System.out.println("[TimetableGenerator] No available class instances found for topic '"
                        + topic.getTopicCode() + " – " + topic.getTopicName()
                        + "'. Cannot generate a timetable.");
                return null;
            }
        }

        Map<String, List<ScoredInstance>> scoredByTopic = new HashMap<>();
        for (Topic topic : selectedTopics) {
            String code = topic.getTopicCode();
            List<ScoredInstance> scored = scoreAndSort(instancesByTopic.get(code), sortedPreferences);
            scoredByTopic.put(code, scored);
        }

        List<String> topicOrder = selectedTopics.stream()
                .map(Topic::getTopicCode)
                .collect(Collectors.toList());

        Result best = new Result();
        backtrack(topicOrder, scoredByTopic, 0, new ArrayList<>(), 0.0, allowLectureOverlap, best);

        if (best.instances == null) {
            System.out.println("[TimetableGenerator] No valid timetable could be generated "
                    + "for the selected topics. Try relaxing campus filters or preferences.");
            return null;
        }

        Timetable timetable = new Timetable(timetableName, semester,
                allowLectureOverlap, new ArrayList<>(normalisedCampuses));
        timetable.setClassInstances(best.instances);

        System.out.println("[TimetableGenerator] Timetable '" + timetableName
                + "' generated successfully with " + best.instances.size()
                + " class instance(s). Total preference score: "
                + String.format("%.1f", best.score));

        return timetable;
    }

    // -------------------------------------------------------------------------
    // Backtracking
    // -------------------------------------------------------------------------

    private void backtrack(List<String> topicOrder,
                            Map<String, List<ScoredInstance>> scoredByTopic,
                            int depth,
                            List<ClassInstance> selected,
                            double currentScore,
                            boolean allowOverlap,
                            Result best) {

        if (depth == topicOrder.size()) {
            if (currentScore > best.score) {
                best.score     = currentScore;
                best.instances = new ArrayList<>(selected);
            }
            return;
        }

        String topicCode = topicOrder.get(depth);
        List<ScoredInstance> candidates = scoredByTopic.get(topicCode);

        for (ScoredInstance candidate : candidates) {
            ClassInstance instance = candidate.instance;
            if (conflictsWithSelected(instance, selected, allowOverlap)) continue;

            selected.add(instance);
            backtrack(topicOrder, scoredByTopic, depth + 1,
                    selected, currentScore + candidate.score, allowOverlap, best);
            selected.remove(selected.size() - 1);
        }
    }

    private boolean conflictsWithSelected(ClassInstance candidate,
                                           List<ClassInstance> selected,
                                           boolean allowOverlap) {
        for (ClassInstance existing : selected) {
            if (allowOverlap && isLecture(candidate) && isLecture(existing)) {
                if (hasTravelConflictOnly(candidate, existing)) return true;
            } else {
                if (conflictDetector.hasConflict(candidate, existing)) return true;
            }
        }
        return false;
    }

    private boolean hasTravelConflictOnly(ClassInstance a, ClassInstance b) {
        List<ConflictDetector.ConflictPair> pairs =
                conflictDetector.getConflicts(Arrays.asList(a, b));
        return pairs.stream().anyMatch(ConflictDetector.ConflictPair::hasTravelConflict);
    }

    // -------------------------------------------------------------------------
    // Preference scoring
    // -------------------------------------------------------------------------

    private List<ScoredInstance> scoreAndSort(List<ClassInstance> candidates,
                                               List<UserPreference> preferences) {
        List<ScoredInstance> scored = new ArrayList<>();
        for (ClassInstance instance : candidates) {
            scored.add(new ScoredInstance(instance, computeScore(instance, preferences)));
        }
        scored.sort(Comparator.comparingDouble(ScoredInstance::getScore).reversed());
        return scored;
    }

    private double computeScore(ClassInstance instance, List<UserPreference> preferences) {
        double score = 0.0;
        for (UserPreference pref : preferences) {
            score += evaluatePreference(instance, pref);
        }
        return score;
    }

    private double evaluatePreference(ClassInstance instance, UserPreference pref) {
        if (pref.getPreferenceType() == null) return 0.0;

        String type = pref.getPreferenceType().trim().toUpperCase();
        int order   = Math.max(1, pref.getPriorityOrder());
        double pos  = PREFERENCE_SCORE_STEP / order;
        double neg  = AVOID_PENALTY / order;

        if (type.startsWith("PREFER_CAMPUS:")) {
            String preferred = extractParam(type);
            return (instance.getCampus() != null
                    && instance.getCampus().equalsIgnoreCase(preferred)) ? pos : 0.0;
        }
        if (type.startsWith("AVOID_CAMPUS:")) {
            String avoided = extractParam(type);
            return (instance.getCampus() != null
                    && instance.getCampus().equalsIgnoreCase(avoided)) ? neg : 0.0;
        }
        if (type.startsWith("PREFER_FORMAT:")) {
            String preferred = extractParam(type);
            return (instance.getClassFormat() != null
                    && instance.getClassFormat().equalsIgnoreCase(preferred)) ? pos : 0.0;
        }
        if (type.startsWith("AVOID_FORMAT:")) {
            String avoided = extractParam(type);
            return (instance.getClassFormat() != null
                    && instance.getClassFormat().equalsIgnoreCase(avoided)) ? neg : 0.0;
        }
        if (type.equals("MINIMIZE_GAPS"))    return hasLargeGaps(instance) ? 0.0 : pos;
        if (type.equals("PREFER_MORNING"))   return allSessionsBefore(instance, 12, 0) ? pos : 0.0;
        if (type.equals("PREFER_AFTERNOON")) return allSessionsFrom(instance, 12, 0)   ? pos : 0.0;

        return 0.0;
    }

    // -------------------------------------------------------------------------
    // Preference helpers
    // -------------------------------------------------------------------------

    private String extractParam(String type) {
        int colon = type.indexOf(':');
        if (colon < 0 || colon == type.length() - 1) return "";
        return type.substring(colon + 1).trim();
    }

    private boolean hasLargeGaps(ClassInstance instance) {
        Map<String, List<Session>> byDay = new HashMap<>();
        for (Session s : instance.getSessions()) {
            byDay.computeIfAbsent(s.getDayOfWeek(), k -> new ArrayList<>()).add(s);
        }
        for (List<Session> daySessions : byDay.values()) {
            daySessions.sort(Comparator.comparing(Session::getStartTime));
            for (int i = 0; i < daySessions.size() - 1; i++) {
                long gap = Duration.between(
                        daySessions.get(i).getEndTime(),
                        daySessions.get(i + 1).getStartTime()).toMinutes();
                if (gap > GAP_THRESHOLD_MINUTES) return true;
            }
        }
        return false;
    }

    private boolean allSessionsBefore(ClassInstance instance, int hour, int minute) {
        LocalTime threshold = LocalTime.of(hour, minute);
        return instance.getSessions().stream()
                .allMatch(s -> s.getStartTime() != null && s.getStartTime().isBefore(threshold));
    }

    private boolean allSessionsFrom(ClassInstance instance, int hour, int minute) {
        LocalTime threshold = LocalTime.of(hour, minute);
        return instance.getSessions().stream()
                .allMatch(s -> s.getStartTime() != null && !s.getStartTime().isBefore(threshold));
    }

    // -------------------------------------------------------------------------
    // Grouping and filtering
    // -------------------------------------------------------------------------

    private Map<String, List<ClassInstance>> groupInstancesByTopic(
            List<Topic> selectedTopics,
            List<ClassInstance> availableInstances,
            List<String> normalisedCampuses) {

        Set<String> selectedCodes = selectedTopics.stream()
                .map(Topic::getTopicCode)
                .collect(Collectors.toSet());

        // Group instances by their stored topicCode, filtered to selected topics and campuses
        Map<String, List<ClassInstance>> grouped = new HashMap<>();
        for (String code : selectedCodes) {
            grouped.put(code, new ArrayList<>());
        }

        for (ClassInstance instance : availableInstances) {
            String code = instance.getTopicCode();
            if (code == null || !selectedCodes.contains(code)) continue;

            if (!normalisedCampuses.isEmpty()
                    && !isOnline(instance)
                    && (instance.getCampus() == null
                    || !normalisedCampuses.contains(instance.getCampus().toLowerCase()))) {
                continue;
            }

            grouped.get(code).add(instance);
        }

        return grouped;
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    private boolean isLecture(ClassInstance instance) {
        return "Lecture".equalsIgnoreCase(instance.getClassFormat());
    }

    private boolean isOnline(ClassInstance instance) {
        return instance.getAttendanceMode() != null
                && instance.getAttendanceMode().toLowerCase().contains("online");
    }

    private List<UserPreference> sortPreferences(List<UserPreference> preferences) {
        if (preferences == null) return new ArrayList<>();
        return preferences.stream()
                .sorted(Comparator.comparingInt(UserPreference::getPriorityOrder))
                .collect(Collectors.toList());
    }

    private List<String> normaliseCampuses(List<String> campuses) {
        if (campuses == null) return new ArrayList<>();
        return campuses.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Internal data holders
    // -------------------------------------------------------------------------

    private static class ScoredInstance {
        final ClassInstance instance;
        final double score;

        ScoredInstance(ClassInstance instance, double score) {
            this.instance = instance;
            this.score    = score;
        }

        double getScore() { return score; }
    }

    private static class Result {
        List<ClassInstance> instances = null;
        double score = Double.NEGATIVE_INFINITY;
    }
}
