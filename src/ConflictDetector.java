import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects scheduling conflicts between ClassInstance objects.
 *
 * <p>Two class instances conflict if any of their sessions overlap on the same day,
 * either because their time windows intersect directly, or because the travel gap
 * between different campuses is insufficient (less than {@value #TRAVEL_BUFFER_MINUTES} minutes).
 */
public class ConflictDetector {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Minimum required gap in minutes when consecutive sessions are on different campuses. */
    public static final int TRAVEL_BUFFER_MINUTES = 30;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Checks whether two ClassInstance objects have any scheduling conflict.
     *
     * @param a first ClassInstance
     * @param b second ClassInstance
     * @return {@code true} if the instances conflict, {@code false} otherwise
     */
    public boolean hasConflict(ClassInstance a, ClassInstance b) {
        for (Session sessionA : a.getSessions()) {
            for (Session sessionB : b.getSessions()) {
                if (sessionsConflict(sessionA, sessionB, a.getCampus(), b.getCampus())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds all conflicting pairs within a list of ClassInstance objects.
     *
     * @param instances list of ClassInstance objects to check
     * @return list of all conflicting pairs; empty if no conflicts exist
     */
    public List<ConflictPair> getConflicts(List<ClassInstance> instances) {
        List<ConflictPair> conflicts = new ArrayList<>();

        for (int i = 0; i < instances.size(); i++) {
            for (int j = i + 1; j < instances.size(); j++) {
                ClassInstance a = instances.get(i);
                ClassInstance b = instances.get(j);

                List<ConflictDetail> details = findConflictDetails(a, b);
                if (!details.isEmpty()) {
                    conflicts.add(new ConflictPair(a, b, details));
                }
            }
        }

        return conflicts;
    }

    // -------------------------------------------------------------------------
    // Core conflict logic
    // -------------------------------------------------------------------------

    private List<ConflictDetail> findConflictDetails(ClassInstance a, ClassInstance b) {
        List<ConflictDetail> details = new ArrayList<>();

        for (Session sessionA : a.getSessions()) {
            for (Session sessionB : b.getSessions()) {
                if (!sameDay(sessionA, sessionB)) {
                    continue;
                }

                if (timesOverlap(sessionA.getStartTime(), sessionA.getEndTime(),
                        sessionB.getStartTime(), sessionB.getEndTime())) {

                    details.add(new ConflictDetail(
                            ConflictType.TIME_OVERLAP,
                            sessionA,
                            sessionB,
                            "Sessions overlap on " + sessionA.getDayOfWeek()
                                    + " (" + sessionA.getStartTime() + "–" + sessionA.getEndTime()
                                    + " vs " + sessionB.getStartTime() + "–" + sessionB.getEndTime() + ")"
                    ));

                } else if (differentCampuses(a.getCampus(), b.getCampus())
                        && insufficientTravelGap(sessionA.getStartTime(), sessionA.getEndTime(),
                        sessionB.getStartTime(), sessionB.getEndTime())) {

                    int gap = gapBetweenMinutes(sessionA.getStartTime(), sessionA.getEndTime(),
                            sessionB.getStartTime(), sessionB.getEndTime());
                    details.add(new ConflictDetail(
                            ConflictType.TRAVEL_TIME,
                            sessionA,
                            sessionB,
                            "Insufficient travel time on " + sessionA.getDayOfWeek()
                                    + ": only " + gap + " min between "
                                    + a.getCampus() + " and " + b.getCampus()
                                    + " (need " + TRAVEL_BUFFER_MINUTES + " min)"
                    ));
                }
            }
        }

        return details;
    }

    private boolean sessionsConflict(Session a, Session b, String campusA, String campusB) {
        if (!sameDay(a, b)) return false;
        if (timesOverlap(a.getStartTime(), a.getEndTime(), b.getStartTime(), b.getEndTime())) {
            return true;
        }
        return differentCampuses(campusA, campusB)
                && insufficientTravelGap(a.getStartTime(), a.getEndTime(),
                b.getStartTime(), b.getEndTime());
    }

    // -------------------------------------------------------------------------
    // Helper predicates
    // -------------------------------------------------------------------------

    private boolean sameDay(Session a, Session b) {
        return a.getDayOfWeek() != null
                && a.getDayOfWeek().equalsIgnoreCase(b.getDayOfWeek());
    }

    private boolean timesOverlap(LocalTime startA, LocalTime endA,
                                  LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private boolean differentCampuses(String campusA, String campusB) {
        if (campusA == null || campusA.isBlank() || campusB == null || campusB.isBlank()) {
            return false;
        }
        return !campusA.equalsIgnoreCase(campusB);
    }

    private boolean insufficientTravelGap(LocalTime startA, LocalTime endA,
                                           LocalTime startB, LocalTime endB) {
        int gap = gapBetweenMinutes(startA, endA, startB, endB);
        return gap >= 0 && gap < TRAVEL_BUFFER_MINUTES;
    }

    private int gapBetweenMinutes(LocalTime startA, LocalTime endA,
                                   LocalTime startB, LocalTime endB) {
        if (timesOverlap(startA, endA, startB, endB)) return -1;
        if (!endA.isAfter(startB)) return (int) Duration.between(endA, startB).toMinutes();
        return (int) Duration.between(endB, startA).toMinutes();
    }

    // =========================================================================
    // Nested types
    // =========================================================================

    public enum ConflictType {
        TIME_OVERLAP,
        TRAVEL_TIME
    }

    public static class ConflictDetail {
        private final ConflictType type;
        private final Session sessionA;
        private final Session sessionB;
        private final String description;

        public ConflictDetail(ConflictType type, Session sessionA,
                              Session sessionB, String description) {
            this.type        = type;
            this.sessionA    = sessionA;
            this.sessionB    = sessionB;
            this.description = description;
        }

        public ConflictType getType()       { return type; }
        public Session getSessionA()        { return sessionA; }
        public Session getSessionB()        { return sessionB; }
        public String getDescription()      { return description; }

        @Override
        public String toString() {
            return "[" + type + "] " + description;
        }
    }

    public static class ConflictPair {
        private final ClassInstance instanceA;
        private final ClassInstance instanceB;
        private final List<ConflictDetail> details;

        public ConflictPair(ClassInstance instanceA, ClassInstance instanceB,
                            List<ConflictDetail> details) {
            this.instanceA = instanceA;
            this.instanceB = instanceB;
            this.details   = details;
        }

        public ClassInstance getInstanceA()       { return instanceA; }
        public ClassInstance getInstanceB()       { return instanceB; }
        public List<ConflictDetail> getDetails()  { return details; }

        public boolean hasTimeOverlap() {
            return details.stream().anyMatch(d -> d.getType() == ConflictType.TIME_OVERLAP);
        }

        public boolean hasTravelConflict() {
            return details.stream().anyMatch(d -> d.getType() == ConflictType.TRAVEL_TIME);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ConflictPair{\n");
            sb.append("  instanceA: ").append(instanceA.getClassFormat())
              .append(" @ ").append(instanceA.getCampus()).append("\n");
            sb.append("  instanceB: ").append(instanceB.getClassFormat())
              .append(" @ ").append(instanceB.getCampus()).append("\n");
            for (ConflictDetail detail : details) {
                sb.append("  -> ").append(detail).append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }
}
