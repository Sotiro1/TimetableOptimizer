import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Entry point for the Student Timetable Optimizer.
 *
 * <p>Presents a menu-driven console interface that guides the student through:
 * <ol>
 *   <li>Importing class data from a CSV file</li>
 *   <li>Browsing all available classes</li>
 *   <li>Viewing detailed class information</li>
 *   <li>Searching classes by criteria</li>
 *   <li>Editing class data</li>
 *   <li>Deleting class data</li>
 *   <li>Selecting topics to enrol in</li>
 *   <li>Setting scheduling preferences</li>
 *   <li>Generating an optimised timetable</li>
 *   <li>Browsing saved timetables</li>
 *   <li>Viewing a timetable in detail</li>
 *   <li>Editing a timetable (swap class instances)</li>
 *   <li>Deleting a timetable</li>
 *   <li>Exporting a timetable to XLSX</li>
 *   <li>Exiting the application</li>
 * </ol>
 */
public class Main {

    // =========================================================================
    // ANSI colour codes
    // =========================================================================

    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String WHITE   = "\u001B[37m";

    // =========================================================================
    // Menu option constants
    // =========================================================================

    private static final int MENU_IMPORT        = 1;
    private static final int MENU_BROWSE        = 2;
    private static final int MENU_VIEW          = 3;
    private static final int MENU_SEARCH        = 4;
    private static final int MENU_EDIT_CLASS    = 5;
    private static final int MENU_DELETE_CLASS  = 6;
    private static final int MENU_TOPICS        = 7;
    private static final int MENU_PREFS         = 8;
    private static final int MENU_GENERATE      = 9;
    private static final int MENU_BROWSE_TT     = 10;
    private static final int MENU_VIEW_TT       = 11;
    private static final int MENU_EDIT_TT       = 12;
    private static final int MENU_DELETE_TT     = 13;
    private static final int MENU_EXPORT        = 14;
    private static final int MENU_EXIT          = 15;

    // =========================================================================
    // Application state
    // =========================================================================

    private List<ClassInstance> availableInstances = new ArrayList<>();
    private Map<String, Topic>  availableTopics    = new LinkedHashMap<>();
    private List<Topic>         selectedTopics     = new ArrayList<>();
    private List<UserPreference> preferences       = new ArrayList<>();

    /** All saved timetables, keyed by unique name. */
    private Map<String, Timetable> timetables = new LinkedHashMap<>();

    // =========================================================================
    // Services
    // =========================================================================

    private final CSVImporter        importer  = new CSVImporter();
    private final TimetableGenerator generator = new TimetableGenerator();
    private final TimetableExporter  exporter  = new TimetableExporter();
    private final ConflictDetector   detector  = new ConflictDetector();
    private final Scanner            scanner   = new Scanner(System.in);

    // =========================================================================
    // Entry point
    // =========================================================================

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readIntInRange("Enter your choice (1-15): ", 1, 15);

            switch (choice) {
                case MENU_IMPORT:       handleImport();       break;
                case MENU_BROWSE:       handleBrowse();       break;
                case MENU_VIEW:         handleView();         break;
                case MENU_SEARCH:       handleSearch();       break;
                case MENU_EDIT_CLASS:   handleEditClass();    break;
                case MENU_DELETE_CLASS: handleDeleteClass();  break;
                case MENU_TOPICS:       handleTopics();       break;
                case MENU_PREFS:        handlePrefs();        break;
                case MENU_GENERATE:     handleGenerate();     break;
                case MENU_BROWSE_TT:    handleBrowseTimetables();  break;
                case MENU_VIEW_TT:      handleViewTimetable();     break;
                case MENU_EDIT_TT:      handleEditTimetable();     break;
                case MENU_DELETE_TT:    handleDeleteTimetable();   break;
                case MENU_EXPORT:       handleExport();       break;
                case MENU_EXIT:
                    System.out.println(CYAN + "\n  Goodbye! Good luck with your studies." + RESET);
                    running = false;
                    break;
            }
        }

        scanner.close();
    }

    // =========================================================================
    // 1. Import class data
    // =========================================================================

    private void handleImport() {
        printSectionHeader("Import Class Data from CSV");

        String filePath = readNonEmptyString("  Enter path to CSV file: ");

        System.out.println();
        List<ClassInstance> instances = importer.importFromCSV(filePath);

        if (instances.isEmpty()) {
            printWarning("No class instances were loaded. Check the file path and CSV format.");
            return;
        }

        availableInstances = instances;
        availableTopics.clear();
        for (Topic topic : importer.getTopics()) {
            availableTopics.put(topic.getTopicCode(), topic);
        }

        selectedTopics.clear();

        System.out.println("  Loaded " + BOLD + availableTopics.size() + RESET
                + " topic(s) and " + BOLD + availableInstances.size() + RESET
                + " class instance(s).");
        printSuccess("Data import complete.");
    }

    // =========================================================================
    // 2. Browse classes
    // =========================================================================

    private void handleBrowse() {
        printSectionHeader("Browse Classes");

        if (!guardDataImported()) return;

        // Group by topic → show combined summary
        Map<String, List<ClassInstance>> byTopic = groupByTopic(availableInstances);

        System.out.println(BOLD + CYAN
                + String.format("  %-12s %-40s %-20s %-15s %s",
                "Topic Code", "Topic Name", "Campus", "Semester", "Classes")
                + RESET);
        printDivider(110);

        for (Map.Entry<String, List<ClassInstance>> entry : byTopic.entrySet()) {
            List<ClassInstance> group = entry.getValue();
            ClassInstance first = group.get(0);

            // Collect unique campuses and class formats for the summary
            String campuses = group.stream()
                    .map(ClassInstance::getCampus)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .collect(Collectors.joining(", "));
            String semester = first.getSemester() != null ? first.getSemester() : "";
            String formats = group.stream()
                    .map(ClassInstance::getClassFormat)
                    .filter(f -> f != null && !f.isBlank())
                    .distinct()
                    .collect(Collectors.joining(", "));

            System.out.printf("  %-12s %-40s %-20s %-15s %s%n",
                    safe(first.getTopicCode()),
                    truncate(safe(first.getTopicName()), 38),
                    truncate(campuses, 18),
                    semester,
                    formats);
        }

        printDivider(110);
        System.out.println("  Total: " + byTopic.size() + " topic(s), "
                + availableInstances.size() + " class instance(s).");
    }

    // =========================================================================
    // 3. View classes (detailed)
    // =========================================================================

    private void handleView() {
        printSectionHeader("View Class Details");

        if (!guardDataImported()) return;

        Map<String, List<ClassInstance>> byTopic = groupByTopic(availableInstances);
        List<String> topicCodes = new ArrayList<>(byTopic.keySet());

        System.out.println("  Select a topic to view (or 0 to view all):");
        for (int i = 0; i < topicCodes.size(); i++) {
            String code = topicCodes.get(i);
            ClassInstance first = byTopic.get(code).get(0);
            System.out.printf("    %2d. %s – %s%n", i + 1, code, safe(first.getTopicName()));
        }

        int choice = readIntInRange("  Your choice (0 = all): ", 0, topicCodes.size());

        List<ClassInstance> toShow;
        if (choice == 0) {
            toShow = availableInstances;
        } else {
            toShow = byTopic.get(topicCodes.get(choice - 1));
        }

        printDetailedClassList(toShow);
    }

    private void printDetailedClassList(List<ClassInstance> instances) {
        System.out.println();
        for (ClassInstance ci : instances) {
            System.out.println(BOLD + CYAN + "  ┌─ " + safe(ci.getTopicCode())
                    + " – " + safe(ci.getTopicName()) + RESET);
            System.out.println("  │  Attendance : " + safe(ci.getAttendanceMode()));
            System.out.println("  │  Campus     : " + safe(ci.getCampus()));
            System.out.println("  │  Semester   : " + safe(ci.getSemester()));
            System.out.println("  │  Avail. No. : " + ci.getAvailabilityNumber());
            System.out.println("  │  Format     : " + safe(ci.getClassFormat())
                    + "  (Instance " + ci.getInstanceNumber() + ")");
            System.out.println("  │  Sessions:");
            for (Session s : ci.getSessions()) {
                System.out.printf("  │    %-12s %s – %s   %s → %s   %s, %s%n",
                        safe(s.getDayOfWeek()),
                        s.getStartTime() != null ? s.getStartTime() : "?",
                        s.getEndTime()   != null ? s.getEndTime()   : "?",
                        s.getFirstClassDate() != null ? s.getFirstClassDate() : "?",
                        s.getLastClassDate()  != null ? s.getLastClassDate()  : "?",
                        safe(s.getBuilding()),
                        safe(s.getRoom()));
            }
            System.out.println("  └" + "─".repeat(70));
        }
    }

    // =========================================================================
    // 4. Search classes
    // =========================================================================

    private void handleSearch() {
        printSectionHeader("Search Classes");

        if (!guardDataImported()) return;

        System.out.println("  Enter search criteria (leave blank to skip a field):");
        System.out.println(YELLOW + "  Tip: all criteria must match (AND logic). "
                + "Leave everything blank to show all." + RESET);
        System.out.println();

        String topicCode      = readOptional("  Topic code       : ");
        String topicName      = readOptional("  Topic name       : ");
        String attendanceMode = readOptional("  Attendance mode  : ");
        String campus         = readOptional("  Campus           : ");
        String semester       = readOptional("  Semester         : ");
        String classFormat    = readOptional("  Class format     : ");
        String day            = readOptional("  Day of week      : ");
        String building       = readOptional("  Building         : ");
        String room           = readOptional("  Room             : ");
        String startTime      = readOptional("  Start time (H:mm): ");
        String endTime        = readOptional("  End time   (H:mm): ");

        List<ClassInstance> results = availableInstances.stream()
                .filter(ci -> matchesCI(ci, topicCode, topicName, attendanceMode,
                        campus, semester, classFormat, day, building, room, startTime, endTime))
                .collect(Collectors.toList());

        System.out.println();
        if (results.isEmpty()) {
            printWarning("No classes matched your search criteria.");
        } else {
            System.out.println(GREEN + "  Found " + results.size() + " match(es):" + RESET);
            printDetailedClassList(results);
        }
    }

    private boolean matchesCI(ClassInstance ci,
                              String topicCode, String topicName,
                              String attendanceMode, String campus,
                              String semester, String classFormat,
                              String day, String building, String room,
                              String startTime, String endTime) {
        if (!matchField(ci.getTopicCode(),      topicCode))      return false;
        if (!matchField(ci.getTopicName(),      topicName))      return false;
        if (!matchField(ci.getAttendanceMode(), attendanceMode)) return false;
        if (!matchField(ci.getCampus(),         campus))         return false;
        if (!matchField(ci.getSemester(),       semester))       return false;
        if (!matchField(ci.getClassFormat(),    classFormat))    return false;

        // Session-level filtering — match if ANY session meets criteria
        boolean sessionCriteriaPresent = !day.isEmpty() || !building.isEmpty()
                || !room.isEmpty() || !startTime.isEmpty() || !endTime.isEmpty();

        if (sessionCriteriaPresent) {
            boolean anyMatch = ci.getSessions().stream().anyMatch(s ->
                    matchField(s.getDayOfWeek(), day)
                            && matchField(s.getBuilding(), building)
                            && matchField(s.getRoom(), room)
                            && matchField(s.getStartTime() != null ? s.getStartTime().toString() : "", startTime)
                            && matchField(s.getEndTime()   != null ? s.getEndTime().toString()   : "", endTime));
            if (!anyMatch) return false;
        }

        return true;
    }

    private boolean matchField(String value, String criterion) {
        if (criterion == null || criterion.isEmpty()) return true;
        if (value == null) return false;
        return value.toLowerCase().contains(criterion.toLowerCase());
    }

    // =========================================================================
    // 5. Edit class
    // =========================================================================

    private void handleEditClass() {
        printSectionHeader("Edit Class");

        if (!guardDataImported()) return;

        ClassInstance ci = selectClassInstance("select a class to edit");
        if (ci == null) return;

        System.out.println();
        System.out.println("  Editing: " + BOLD + ci.getTopicCode()
                + " – " + ci.getTopicName() + " [" + ci.getClassFormat()
                + " #" + ci.getInstanceNumber() + "]" + RESET);
        System.out.println("  Leave a field blank to keep the current value.");
        System.out.println();

        System.out.println("  Current topic code: " + safe(ci.getTopicCode()));
        String newCode = readOptional("  New topic code     : ");
        if (!newCode.isEmpty()) ci.setTopicCode(newCode);

        System.out.println("  Current topic name: " + safe(ci.getTopicName()));
        String newName = readOptional("  New topic name     : ");
        if (!newName.isEmpty()) ci.setTopicName(newName);

        System.out.println("  Current campus: " + safe(ci.getCampus()));
        String newCampus = readOptional("  New campus         : ");
        if (!newCampus.isEmpty()) ci.setCampus(newCampus);

        System.out.println("  Current semester: " + safe(ci.getSemester()));
        String newSemester = readOptional("  New semester       : ");
        if (!newSemester.isEmpty()) ci.setSemester(newSemester);

        System.out.println("  Current class format: " + safe(ci.getClassFormat()));
        String newFormat = readOptional("  New class format   : ");
        if (!newFormat.isEmpty()) ci.setClassFormat(newFormat);

        System.out.println("  Current attendance mode: " + safe(ci.getAttendanceMode()));
        String newMode = readOptional("  New attendance mode: ");
        if (!newMode.isEmpty()) ci.setAttendanceMode(newMode);

        System.out.println();
        System.out.println(YELLOW + "  ⚠  WARNING: You are about to save these changes." + RESET);
        boolean confirmed = readYesNo("  Confirm changes? (y/n): ");
        if (confirmed) {
            // Update topic name in topic map if code changed
            if (!newCode.isEmpty()) {
                availableTopics.put(ci.getTopicCode(),
                        new Topic(ci.getTopicCode(), ci.getTopicName()));
            }
            printSuccess("Class updated successfully.");
        } else {
            printWarning("Edit cancelled. No changes were saved.");
        }
    }

    // =========================================================================
    // 6. Delete class
    // =========================================================================

    private void handleDeleteClass() {
        printSectionHeader("Delete Class");

        if (!guardDataImported()) return;

        ClassInstance ci = selectClassInstance("select a class to delete");
        if (ci == null) return;

        System.out.println();
        System.out.println("  You are about to delete:");
        System.out.println("  " + BOLD + ci.getTopicCode() + " – " + ci.getTopicName()
                + " [" + ci.getClassFormat() + " #" + ci.getInstanceNumber() + "]" + RESET);
        System.out.println(RED + "  ⚠  WARNING: This action cannot be undone." + RESET);

        boolean confirmed = readYesNo("  Are you sure you want to delete this class? (y/n): ");
        if (confirmed) {
            availableInstances.remove(ci);
            // Remove topic if no more instances exist for it
            boolean topicStillExists = availableInstances.stream()
                    .anyMatch(i -> ci.getTopicCode().equals(i.getTopicCode()));
            if (!topicStillExists) {
                availableTopics.remove(ci.getTopicCode());
                selectedTopics.removeIf(t -> t.getTopicCode().equals(ci.getTopicCode()));
            }
            printSuccess("Class instance deleted.");
        } else {
            printWarning("Deletion cancelled.");
        }
    }

    // =========================================================================
    // 7. Select topics
    // =========================================================================

    private void handleTopics() {
        printSectionHeader("Select Topics");

        if (!guardDataImported()) return;

        List<Topic> topicList = new ArrayList<>(availableTopics.values());
        System.out.println("  Available topics:");
        for (int i = 0; i < topicList.size(); i++) {
            Topic t = topicList.get(i);
            boolean sel = selectedTopics.stream()
                    .anyMatch(s -> s.getTopicCode().equals(t.getTopicCode()));
            String indicator = sel ? GREEN + " [SELECTED]" + RESET : "";
            System.out.printf("    %2d. %s – %s%s%n",
                    i + 1, t.getTopicCode(), t.getTopicName(), indicator);
        }

        System.out.println();
        System.out.println("  Enter topic numbers separated by spaces (e.g. 1 3 4),");
        System.out.println("  or press Enter to keep your current selection.");
        System.out.print("  Your choice: ");
        String line = scanner.nextLine().trim();

        if (line.isEmpty()) {
            if (selectedTopics.isEmpty()) {
                printWarning("No topics are currently selected.");
            } else {
                System.out.println("  Selection unchanged: "
                        + selectedTopics.size() + " topic(s) selected.");
            }
            return;
        }

        String[] tokens = line.split("\\s+");
        List<Topic> newSelection = new ArrayList<>();
        boolean hasError = false;

        for (String token : tokens) {
            Integer index = parsePositiveInt(token);
            if (index == null || index < 1 || index > topicList.size()) {
                printError("'" + token + "' is not a valid topic number.");
                hasError = true;
                continue;
            }
            Topic chosen = topicList.get(index - 1);
            if (newSelection.stream().noneMatch(t -> t.getTopicCode().equals(chosen.getTopicCode()))) {
                newSelection.add(chosen);
            }
        }

        if (hasError) {
            printWarning("Some entries were invalid and were ignored.");
        }

        if (newSelection.isEmpty()) {
            printError("No valid topics were selected. Previous selection unchanged.");
            return;
        }

        selectedTopics = newSelection;
        System.out.println();
        System.out.println("  Selected topics:");
        for (Topic t : selectedTopics) {
            System.out.println("    " + GREEN + "✔" + RESET + "  "
                    + t.getTopicCode() + " – " + t.getTopicName());
        }
        printSuccess(selectedTopics.size() + " topic(s) selected.");
    }

    // =========================================================================
    // 8. Set preferences
    // =========================================================================

    private void handlePrefs() {
        printSectionHeader("Set Preferences");

        boolean managing = true;
        while (managing) {
            printPreferencesMenu();
            int choice = readIntInRange("  Choice: ", 1, 8);

            switch (choice) {
                case 1: addCampusPreference(true);        break;
                case 2: addCampusPreference(false);       break;
                case 3: addFormatPreference(true);        break;
                case 4: addFormatPreference(false);       break;
                case 5: addSimplePreference("PREFER_MORNING");    break;
                case 6: addSimplePreference("PREFER_AFTERNOON");  break;
                case 7: addSimplePreference("MINIMIZE_GAPS");     break;
                case 8: managing = false;                 break;
            }
        }
    }

    private void printPreferencesMenu() {
        System.out.println();
        if (preferences.isEmpty()) {
            System.out.println("  " + YELLOW + "No preferences set." + RESET);
        } else {
            System.out.println("  Current preferences (priority order):");
            for (int i = 0; i < preferences.size(); i++) {
                UserPreference p = preferences.get(i);
                System.out.printf("    %d. [Priority %d] %s%n",
                        i + 1, p.getPriorityOrder(), p.getPreferenceType());
            }
        }
        System.out.println();
        System.out.println("  Options:");
        System.out.println("    1. Add preferred campus");
        System.out.println("    2. Add campus to avoid");
        System.out.println("    3. Add preferred class format");
        System.out.println("    4. Add class format to avoid");
        System.out.println("    5. Prefer morning classes");
        System.out.println("    6. Prefer afternoon classes");
        System.out.println("    7. Minimise gaps between classes");
        System.out.println("    8. Back to main menu");
    }

    private void addCampusPreference(boolean isPrefer) {
        String verb   = isPrefer ? "prefer"  : "avoid";
        String prefix = isPrefer ? "PREFER_CAMPUS:" : "AVOID_CAMPUS:";
        String campus   = readNonEmptyString("  Enter campus name to " + verb + ": ");
        int priority    = readIntInRange("  Priority (1 = highest): ", 1, 99);
        preferences.add(new UserPreference(prefix + campus, priority));
        printSuccess("Preference added: " + prefix + campus + " (priority " + priority + ").");
    }

    private void addFormatPreference(boolean isPrefer) {
        String verb   = isPrefer ? "prefer"  : "avoid";
        String prefix = isPrefer ? "PREFER_FORMAT:" : "AVOID_FORMAT:";
        System.out.println("  Common formats: Lecture, Tutorial, Practical, Workshop, Seminar");
        String format   = readNonEmptyString("  Enter class format to " + verb + ": ");
        int priority    = readIntInRange("  Priority (1 = highest): ", 1, 99);
        preferences.add(new UserPreference(prefix + format, priority));
        printSuccess("Preference added: " + prefix + format + " (priority " + priority + ").");
    }

    private void addSimplePreference(String type) {
        int priority = readIntInRange("  Priority for " + type + " (1 = highest): ", 1, 99);
        preferences.add(new UserPreference(type, priority));
        printSuccess("Preference added: " + type + " (priority " + priority + ").");
    }

    // =========================================================================
    // 9. Generate timetable
    // =========================================================================

    private void handleGenerate() {
        printSectionHeader("Generate Timetable");

        if (!guardDataImported())   return;
        if (!guardTopicsSelected()) return;

        String name = readNonEmptyString("  Timetable name (must be unique): ");
        if (timetables.containsKey(name)) {
            printError("A timetable named '" + name + "' already exists. Choose a different name.");
            return;
        }

        String semester      = readNonEmptyString("  Semester (e.g. Semester 2 2026): ");
        boolean allowOverlap = readYesNo("  Allow lecture time overlaps? (y/n): ");

        System.out.println("  Enter campuses to include (comma-separated), or Enter for all:");
        System.out.print("  Campuses: ");
        String campusLine = scanner.nextLine().trim();

        List<String> campuses = new ArrayList<>();
        if (!campusLine.isEmpty()) {
            for (String c : campusLine.split(",")) {
                String trimmed = c.trim();
                if (!trimmed.isEmpty()) campuses.add(trimmed);
            }
        }

        System.out.println();
        System.out.println("  Generating with:");
        System.out.println("    Topics   : " + selectedTopics.size());
        System.out.println("    Prefs    : " + preferences.size());
        System.out.println("    Campuses : " + (campuses.isEmpty() ? "All" : String.join(", ", campuses)));
        System.out.println("    Overlaps : " + (allowOverlap ? "Allowed (lectures only)" : "Not allowed"));
        System.out.println();

        Timetable result;
        try {
            result = generator.generateTimetable(name, semester, selectedTopics,
                    availableInstances, preferences, allowOverlap, campuses);
        } catch (IllegalArgumentException e) {
            printError("Generation failed: " + e.getMessage());
            return;
        }

        if (result == null) {
            printWarning("No valid timetable could be generated. "
                    + "Try adjusting campus filters, preferences, or topic selection.");
            return;
        }

        timetables.put(name, result);
        printTimetableSummary(result);
        printSuccess("Timetable '" + name + "' generated and saved.");
    }

    // =========================================================================
    // 10. Browse timetables
    // =========================================================================

    private void handleBrowseTimetables() {
        printSectionHeader("Browse Timetables");

        if (timetables.isEmpty()) {
            printWarning("No timetables have been generated yet. Use option "
                    + MENU_GENERATE + " to generate one.");
            return;
        }

        System.out.println(BOLD + CYAN
                + String.format("  %-25s %-15s %-8s %s",
                "Name", "Semester", "Classes", "Campuses") + RESET);
        printDivider(80);

        int i = 1;
        for (Timetable tt : timetables.values()) {
            String campuses = String.join(", ", tt.getSelectedCampuses());
            if (campuses.isBlank()) campuses = "All";
            System.out.printf("  %2d. %-22s %-15s %-8d %s%n",
                    i++,
                    truncate(tt.getName(), 22),
                    safe(tt.getSemester()),
                    tt.getClassInstances().size(),
                    campuses);
        }
        printDivider(80);
    }

    // =========================================================================
    // 11. View timetable
    // =========================================================================

    private void handleViewTimetable() {
        printSectionHeader("View Timetable");

        Timetable tt = selectTimetable();
        if (tt == null) return;

        printTimetableSummary(tt);

        // Show conflicts
        List<ConflictDetector.ConflictPair> conflicts =
                detector.getConflicts(tt.getClassInstances());
        if (conflicts.isEmpty()) {
            System.out.println("  " + GREEN + "✔  No scheduling conflicts detected." + RESET);
        } else {
            System.out.println("  " + RED + "⚠  " + conflicts.size()
                    + " conflict(s) detected:" + RESET);
            for (ConflictDetector.ConflictPair cp : conflicts) {
                for (ConflictDetector.ConflictDetail detail : cp.getDetails()) {
                    System.out.println("    " + RED + "→ " + detail.getDescription() + RESET);
                }
            }
        }
    }

    // =========================================================================
    // 12. Edit timetable (swap class instances)
    // =========================================================================

    private void handleEditTimetable() {
        printSectionHeader("Edit Timetable");

        Timetable tt = selectTimetable();
        if (tt == null) return;

        List<ClassInstance> current = tt.getClassInstances();
        if (current.isEmpty()) {
            printWarning("This timetable has no class instances to edit.");
            return;
        }

        System.out.println("  Current class instances:");
        for (int i = 0; i < current.size(); i++) {
            ClassInstance ci = current.get(i);
            System.out.printf("    %2d. %s – %s [%s #%d]%n",
                    i + 1,
                    safe(ci.getTopicCode()),
                    safe(ci.getTopicName()),
                    safe(ci.getClassFormat()),
                    ci.getInstanceNumber());
        }

        int ciChoice = readIntInRange("  Select class to replace (number): ", 1, current.size());
        ClassInstance toReplace = current.get(ciChoice - 1);

        // Find alternatives: same topic + same class format, different instance number
        List<ClassInstance> alternatives = availableInstances.stream()
                .filter(ci -> ci.getTopicCode() != null
                        && ci.getTopicCode().equals(toReplace.getTopicCode())
                        && ci.getClassFormat() != null
                        && ci.getClassFormat().equals(toReplace.getClassFormat())
                        && ci.getInstanceNumber() != toReplace.getInstanceNumber())
                .collect(Collectors.toList());

        if (alternatives.isEmpty()) {
            printWarning("No alternative instances found for "
                    + toReplace.getClassFormat() + " of " + toReplace.getTopicCode() + ".");
            return;
        }

        System.out.println("  Available alternatives:");
        for (int i = 0; i < alternatives.size(); i++) {
            ClassInstance alt = alternatives.get(i);
            String sessions = alt.getSessions().stream()
                    .map(s -> s.getDayOfWeek() + " " + s.getStartTime())
                    .collect(Collectors.joining(", "));
            System.out.printf("    %2d. Instance #%d — %s — %s%n",
                    i + 1, alt.getInstanceNumber(), safe(alt.getCampus()), sessions);
        }

        int altChoice = readIntInRange("  Select replacement (number): ", 1, alternatives.size());
        ClassInstance replacement = alternatives.get(altChoice - 1);

        // Check for conflicts with the rest of the timetable
        List<ClassInstance> others = new ArrayList<>(current);
        others.remove(toReplace);
        boolean hasConflict = others.stream()
                .anyMatch(other -> detector.hasConflict(replacement, other));

        if (hasConflict) {
            System.out.println();
            System.out.println(YELLOW + "  ⚠  WARNING: This swap will create a scheduling conflict "
                    + "or insufficient commute time." + RESET);
            boolean proceed = readYesNo("  Proceed anyway? (y/n): ");
            if (!proceed) {
                printWarning("Edit cancelled.");
                return;
            }
        }

        System.out.println();
        System.out.println(YELLOW + "  ⚠  Confirm: Replace instance #"
                + toReplace.getInstanceNumber() + " with instance #"
                + replacement.getInstanceNumber() + "?" + RESET);
        boolean confirmed = readYesNo("  Confirm? (y/n): ");
        if (confirmed) {
            int idx = current.indexOf(toReplace);
            current.set(idx, replacement);
            printSuccess("Class instance swapped successfully.");
        } else {
            printWarning("Edit cancelled.");
        }
    }

    // =========================================================================
    // 13. Delete timetable
    // =========================================================================

    private void handleDeleteTimetable() {
        printSectionHeader("Delete Timetable");

        if (timetables.isEmpty()) {
            printWarning("No timetables to delete.");
            return;
        }

        Timetable tt = selectTimetable();
        if (tt == null) return;

        System.out.println();
        System.out.println("  You are about to delete timetable: "
                + BOLD + tt.getName() + RESET);
        System.out.println(RED + "  ⚠  WARNING: This action cannot be undone." + RESET);

        boolean confirmed = readYesNo("  Are you sure? (y/n): ");
        if (confirmed) {
            timetables.remove(tt.getName());
            printSuccess("Timetable '" + tt.getName() + "' deleted.");
        } else {
            printWarning("Deletion cancelled.");
        }
    }

    // =========================================================================
    // 14. Export timetable
    // =========================================================================

    private void handleExport() {
        printSectionHeader("Export Timetable");

        if (timetables.isEmpty()) {
            printWarning("No timetables to export. Generate one first.");
            return;
        }

        Timetable tt = selectTimetable();
        if (tt == null) return;

        String outputPath = readNonEmptyString("  Output file path (e.g. timetable.xlsx): ");
        if (!outputPath.toLowerCase().endsWith(".xlsx")) {
            outputPath += ".xlsx";
            System.out.println("  (Extension .xlsx added: " + outputPath + ")");
        }

        System.out.println();
        exporter.export(tt, outputPath);
    }

    // =========================================================================
    // Guard checks
    // =========================================================================

    private boolean guardDataImported() {
        if (availableInstances.isEmpty()) {
            printWarning("No class data imported yet. Use option "
                    + MENU_IMPORT + " first.");
            return false;
        }
        return true;
    }

    private boolean guardTopicsSelected() {
        if (selectedTopics.isEmpty()) {
            printWarning("No topics selected yet. Use option "
                    + MENU_TOPICS + " first.");
            return false;
        }
        return true;
    }

    // =========================================================================
    // Shared helpers — instance/timetable pickers
    // =========================================================================

    /**
     * Lets the user pick a ClassInstance from the full list.
     * Returns null if the user cancels (enters 0).
     */
    private ClassInstance selectClassInstance(String purpose) {
        if (availableInstances.isEmpty()) return null;

        System.out.println("  Available class instances (enter 0 to cancel):");
        for (int i = 0; i < availableInstances.size(); i++) {
            ClassInstance ci = availableInstances.get(i);
            System.out.printf("    %3d. %-10s %-35s %-12s #%d%n",
                    i + 1,
                    safe(ci.getTopicCode()),
                    truncate(safe(ci.getTopicName()), 33),
                    safe(ci.getClassFormat()),
                    ci.getInstanceNumber());
        }

        int choice = readIntInRange("  Enter number to " + purpose + " (0 = cancel): ",
                0, availableInstances.size());
        if (choice == 0) {
            printWarning("Cancelled.");
            return null;
        }
        return availableInstances.get(choice - 1);
    }

    /**
     * Lets the user pick a Timetable from the saved list.
     * Returns null if cancelled or none available.
     */
    private Timetable selectTimetable() {
        if (timetables.isEmpty()) {
            printWarning("No timetables available. Generate one first (option "
                    + MENU_GENERATE + ").");
            return null;
        }

        List<Timetable> list = new ArrayList<>(timetables.values());
        System.out.println("  Saved timetables:");
        for (int i = 0; i < list.size(); i++) {
            Timetable tt = list.get(i);
            System.out.printf("    %2d. %s (%s, %d class(es))%n",
                    i + 1, tt.getName(), safe(tt.getSemester()),
                    tt.getClassInstances().size());
        }

        int choice = readIntInRange("  Select timetable (0 = cancel): ", 0, list.size());
        if (choice == 0) {
            printWarning("Cancelled.");
            return null;
        }
        return list.get(choice - 1);
    }

    // =========================================================================
    // Input helpers
    // =========================================================================

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            printError("Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            printError("Input cannot be empty. Please try again.");
        }
    }

    /** Reads an optional string — returns empty string if user just hits Enter. */
    private String readOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no"))  return false;
            printError("Please enter 'y' or 'n'.");
        }
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================================
    // Display helpers
    // =========================================================================

    private void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  +================================================================+");
        System.out.println("  |                                                                |");
        System.out.println("  |        STUDENT  TIMETABLE  OPTIMIZER                          |");
        System.out.println("  |                                                                |");
        System.out.println("  |        Flinders University  |  2026                           |");
        System.out.println("  |                                                                |");
        System.out.println("  +================================================================+");
        System.out.println(RESET);
    }

    private void printMenu() {
        System.out.println();
        System.out.println(BOLD + CYAN + "─────────────────────────────────────────────────────" + RESET);
        System.out.println(BOLD + "  MAIN MENU" + RESET);
        System.out.println(CYAN + "─────────────────────────────────────────────────────" + RESET);

        String imp  = availableInstances.isEmpty() ? "" : GREEN + " ✔" + RESET;
        String top  = selectedTopics.isEmpty() ? "" : GREEN + " ✔ (" + selectedTopics.size() + " selected)" + RESET;
        String pref = preferences.isEmpty() ? "" : GREEN + " ✔ (" + preferences.size() + " set)" + RESET;
        String tts  = timetables.isEmpty() ? "" : GREEN + " ✔ (" + timetables.size() + " saved)" + RESET;

        System.out.println(YELLOW + "  — Class Data —" + RESET);
        System.out.println("   " + MENU_IMPORT      + ". Import class data from CSV" + imp);
        System.out.println("   " + MENU_BROWSE      + ". Browse classes");
        System.out.println("   " + MENU_VIEW        + ". View class details");
        System.out.println("   " + MENU_SEARCH      + ". Search classes");
        System.out.println("   " + MENU_EDIT_CLASS  + ". Edit a class");
        System.out.println("   " + MENU_DELETE_CLASS + ". Delete a class");
        System.out.println(YELLOW + "  — Timetable Setup —" + RESET);
        System.out.println("   " + MENU_TOPICS      + ". Select topics to enrol in" + top);
        System.out.println("   " + MENU_PREFS       + ". Set preferences" + pref);
        System.out.println("   " + MENU_GENERATE    + ". Generate timetable");
        System.out.println(YELLOW + "  — Timetable Management —" + RESET);
        System.out.println("   " + MENU_BROWSE_TT   + ". Browse timetables" + tts);
        System.out.println("   " + MENU_VIEW_TT     + ". View timetable details");
        System.out.println("   " + MENU_EDIT_TT     + ". Edit timetable (swap classes)");
        System.out.println("   " + MENU_DELETE_TT   + ". Delete timetable");
        System.out.println("   " + MENU_EXPORT      + ". Export timetable to XLSX");
        System.out.println(YELLOW + "  — Application —" + RESET);
        System.out.println("   " + MENU_EXIT        + ". Exit");
        System.out.println(CYAN + "─────────────────────────────────────────────────────" + RESET);
    }

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + CYAN + "═══ " + title + " ═══" + RESET);
    }

    private void printTimetableSummary(Timetable timetable) {
        System.out.println();
        System.out.println(BOLD + "  ┌─ Timetable: " + timetable.getName()
                + "  |  " + safe(timetable.getSemester()) + " ─" + RESET);

        if (timetable.getClassInstances().isEmpty()) {
            System.out.println("  │  (no class instances)");
        }

        for (ClassInstance instance : timetable.getClassInstances()) {
            System.out.println("  │");
            System.out.println("  │  " + BOLD + safe(instance.getTopicCode())
                    + " – " + safe(instance.getTopicName()) + RESET);
            System.out.println("  │  Format: " + safe(instance.getClassFormat())
                    + "  |  Campus: " + safe(instance.getCampus())
                    + "  |  Mode: " + safe(instance.getAttendanceMode()));
            System.out.println("  │  Avail#: " + instance.getAvailabilityNumber()
                    + "  |  Instance#: " + instance.getInstanceNumber()
                    + "  |  Semester: " + safe(instance.getSemester()));
            for (Session s : instance.getSessions()) {
                System.out.printf("  │    %-12s %s – %s   %s → %s%n",
                        safe(s.getDayOfWeek()),
                        s.getStartTime() != null ? s.getStartTime() : "?",
                        s.getEndTime()   != null ? s.getEndTime()   : "?",
                        s.getFirstClassDate() != null ? s.getFirstClassDate() : "?",
                        s.getLastClassDate()  != null ? s.getLastClassDate()  : "?");
                System.out.printf("  │    %s %s%n",
                        safe(s.getBuilding()), safe(s.getRoom()));
            }
        }
        System.out.println("  └" + "─".repeat(60));
    }

    private void printDivider(int width) {
        System.out.println(CYAN + "  " + "─".repeat(width) + RESET);
    }

    private void printSuccess(String message) {
        System.out.println();
        System.out.println("  " + GREEN + "✔  " + message + RESET);
    }

    private void printWarning(String message) {
        System.out.println();
        System.out.println("  " + YELLOW + "⚠  WARNING: " + message + RESET);
    }

    private void printError(String message) {
        System.out.println();
        System.out.println("  " + RED + "✘  ERROR: " + message + RESET);
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private Map<String, List<ClassInstance>> groupByTopic(List<ClassInstance> instances) {
        Map<String, List<ClassInstance>> map = new LinkedHashMap<>();
        for (ClassInstance ci : instances) {
            String code = ci.getTopicCode() != null ? ci.getTopicCode() : "(unknown)";
            map.computeIfAbsent(code, k -> new ArrayList<>()).add(ci);
        }
        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}