import org.junit.jupiter.api.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class CameronTests {

    private static ClassInstance buildClassInstance(String topicCode, String format,
                                                    String campus, String day,
                                                    LocalTime start, LocalTime end) {
        ClassInstance ci = new ClassInstance();
        ci.setTopicCode(topicCode);
        ci.setTopicName("Test Topic");
        ci.setAttendanceMode("On Campus");
        ci.setCampus(campus);
        ci.setSemester("Semester 1 2025");
        ci.setAvailabilityNumber(1);
        ci.setClassFormat(format);
        ci.setInstanceNumber(1);

        Session s = new Session();
        s.setDayOfWeek(day);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setFirstClassDate(LocalDate.of(2025, 3, 3));
        s.setLastClassDate(LocalDate.of(2025, 6, 2));
        s.setBuilding("Building A");
        s.setRoom("101");
        ci.addSession(s);

        return ci;
    }

    @Test
    @Tag("Cameron")
    @Tag("Additional")
    @DisplayName("1.02: Verify that all software inputs and outputs are contained completely within the console terminall")
    void allInputsAndOutputsAreConsoleOnly() throws IOException {

        // Save streams for restore
        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;
        final InputStream realIn  = System.in;

        ByteArrayOutputStream capOut;
        ByteArrayOutputStream capErr;
        String out;
        String err;

        // 1. CSVImporter
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        CSVImporter importer = new CSVImporter();
        List<ClassInstance> importResult = importer.importFromCSV("nonexistent_file_xyz.csv");

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertNotNull(importResult,
                "CSVImporter.importFromCSV must not return null");
        assertTrue(importResult.isEmpty(),
                "CSVImporter must return an empty list for a missing file");
        assertFalse(out.isBlank(),
                "CSVImporter must write a message to System.out when the file is missing");
        assertTrue(out.contains("[CSVImporter]"),
                "CSVImporter output must carry the [CSVImporter] prefix; got: " + out);
        assertTrue(err.isBlank(),
                "CSVImporter must not write to System.err; got: " + err);

        // 2. TimetableGenerator (failure)
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        TimetableGenerator generator = new TimetableGenerator();
        Timetable failResult = generator.generateTimetable(
                "T", "Semester 1 2025",
                List.of(new Topic("COMP1234", "Algorithms")),
                new ArrayList<>(), new ArrayList<>(), false, new ArrayList<>());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertNull(failResult,
                "TimetableGenerator must return null when no instances are available");
        assertFalse(out.isBlank(),
                "TimetableGenerator must print a message to System.out when it cannot build a timetable");
        assertTrue(out.contains("[TimetableGenerator]"),
                "TimetableGenerator output must carry the [TimetableGenerator] prefix; got: " + out);
        assertTrue(err.isBlank(),
                "TimetableGenerator must not write to System.err; got: " + err);

        // 3. TimetableGenerator (success)
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        ClassInstance ci = buildClassInstance(
                "COMP1234", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));
        Timetable successResult = generator.generateTimetable(
                "My Timetable", "Semester 1 2025",
                List.of(new Topic("COMP1234", "Algorithms")),
                List.of(ci), new ArrayList<>(), false, new ArrayList<>());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNotNull(successResult,
                "TimetableGenerator must return a timetable when a valid combination exists");
        assertTrue(out.contains("[TimetableGenerator]"),
                "TimetableGenerator success message must appear on System.out; got: " + out);
        assertTrue(out.contains("generated successfully"),
                "TimetableGenerator success message must confirm generation; got: " + out);

        // 4. TimetableExporter: null timetable → error on System.err
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        TimetableExporter exporter = new TimetableExporter();
        exporter.export(null, "output_test.csv");

        System.setOut(realOut);
        System.setErr(realErr);
        err = capErr.toString();

        assertFalse(err.isBlank(),
                "TimetableExporter must write an error to System.err when timetable is null; got: " + err);

        // 5. TimetableExporter: null file path → error on System.err
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        exporter.export(new Timetable("T", "S1 2025", false, new ArrayList<>()), null);

        System.setOut(realOut);
        System.setErr(realErr);
        err = capErr.toString();

        assertFalse(err.isBlank(),
                "TimetableExporter must write an error to System.err when filePath is null; got: " + err);

        // 6. TimetableExporter: empty timetable → notice on System.out
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        File tmp = File.createTempFile("timetable_test_", ".csv");
        tmp.deleteOnExit();
        exporter.export(new Timetable("EmptyTT", "S1 2025", false, new ArrayList<>()), tmp.getAbsolutePath());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertFalse(out.isBlank(),
                "TimetableExporter must print a notice to System.out for an empty timetable; got: " + out);
        assertTrue(out.contains("[TimetableExporter]"),
                "TimetableExporter output must carry the [TimetableExporter] prefix; got: " + out);

        // 7. ConflictDetector
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        ConflictDetector detector = new ConflictDetector();

        ClassInstance a = buildClassInstance("COMP1000", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));
        ClassInstance b = buildClassInstance("COMP2000", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(10, 0), LocalTime.of(12, 0));
        boolean overlap = detector.hasConflict(a, b);

        ClassInstance c = buildClassInstance("COMP1000", "Tutorial", "City",
                "Tuesday", LocalTime.of(13, 0), LocalTime.of(14, 0));
        ClassInstance d = buildClassInstance("COMP2000", "Tutorial", "Bedford Park",
                "Tuesday", LocalTime.of(14, 10), LocalTime.of(15, 0));
        List<ConflictDetector.ConflictPair> travelConflicts = detector.getConflicts(List.of(c, d));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(overlap,
                "Overlapping sessions on the same campus should be detected as conflicting");
        assertFalse(travelConflicts.isEmpty(),
                "A 10-min gap between different campuses should be flagged as a travel conflict");
        assertTrue(out.isBlank(),
                "ConflictDetector must not write to System.out; got: " + out);
        assertTrue(err.isBlank(),
                "ConflictDetector must not write to System.err; got: " + err);

        // 8. Main: reads from System.in
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));
        System.setIn(new ByteArrayInputStream("15\n".getBytes()));

        Main.main(new String[]{});

        System.setOut(realOut);
        System.setErr(realErr);
        System.setIn(realIn);
        out = capOut.toString();
        err = capErr.toString();

        assertFalse(out.isBlank(),
                "Main must write output to System.out; got nothing");
        assertTrue(out.contains("STUDENT  TIMETABLE  OPTIMIZER"),
                "Main must print the application banner to System.out; got: " + out);
        assertTrue(out.contains("MAIN MENU"),
                "Main must print the main menu to System.out; got: " + out);
        assertTrue(out.contains("Goodbye"),
                "Main must print a goodbye message to System.out after exit; got: " + out);
        assertTrue(err.isBlank(),
                "Main must not write to System.err during normal exit; got: " + err);

        // 9. Main: invalid menu input → error on System.out, nothing on System.err
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));
        System.setIn(new ByteArrayInputStream("99\n15\n".getBytes()));

        Main.main(new String[]{});

        System.setOut(realOut);
        System.setErr(realErr);
        System.setIn(realIn);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(out.contains("ERROR"),
                "An out-of-range menu choice must produce an ERROR message on System.out; got: " + out);
        assertTrue(err.isBlank(),
                "Main must not write to System.err for invalid input; got: " + err);
    }



    @Test
    @Tag("Cameron")
    @Tag("Core")
    @DisplayName("2.03: Verify that importing a CSV file with incorrect formatting outputs an appropriate terminal error message")
    void csvImportWithBadFormattingOutputsTerminalError() throws IOException {

        // Save streams for restore
        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;

        ByteArrayOutputStream capOut;
        ByteArrayOutputStream capErr;
        String out;
        String err;

        // 1. Too few columns — a row with only 3 comma-separated values instead of 15
        File tooFewCols = File.createTempFile("csv_bad_cols_", ".csv");
        tooFewCols.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(tooFewCols)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            pw.println("COMP1234,Algorithms,On Campus");   // only 3 columns
        }

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        CSVImporter importer = new CSVImporter();
        List<ClassInstance> tooFewResult = importer.importFromCSV(tooFewCols.getAbsolutePath());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(tooFewResult.isEmpty(),
                "CSVImporter must load no instances from a row with too few columns");
        assertTrue(out.contains("SKIPPED"),
                "CSVImporter must print a SKIPPED message to System.out for a row with too few columns; got: " + out);
        assertTrue(err.isBlank(),
                "CSVImporter must not write to System.err for a malformed row; got: " + err);

        // 2. Unparseable date — valid column count but the date field is garbage
        File badDate = File.createTempFile("csv_bad_date_", ".csv");
        badDate.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(badDate)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            pw.println("COMP1234,Algorithms,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,NOT-A-DATE,2/06/2025,Monday,9:00,11:00,Building A,101");
        }

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        importer = new CSVImporter();
        List<ClassInstance> badDateResult = importer.importFromCSV(badDate.getAbsolutePath());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(badDateResult.isEmpty(),
                "CSVImporter must load no instances when the First Class Date cannot be parsed");
        assertTrue(out.contains("SKIPPED"),
                "CSVImporter must print a SKIPPED message to System.out for an unparseable date; got: " + out);
        assertTrue(err.isBlank(),
                "CSVImporter must not write to System.err for an unparseable date; got: " + err);

        // 3. Unparseable time — valid column count but the time field is garbage
        File badTime = File.createTempFile("csv_bad_time_", ".csv");
        badTime.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(badTime)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            pw.println("COMP1234,Algorithms,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Monday,NOT-A-TIME,11:00,Building A,101");
        }

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        importer = new CSVImporter();
        List<ClassInstance> badTimeResult = importer.importFromCSV(badTime.getAbsolutePath());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(badTimeResult.isEmpty(),
                "CSVImporter must load no instances when the Start Time cannot be parsed");
        assertTrue(out.contains("SKIPPED"),
                "CSVImporter must print a SKIPPED message to System.out for an unparseable time; got: " + out);
        assertTrue(err.isBlank(),
                "CSVImporter must not write to System.err for an unparseable time; got: " + err);

        // 4. Missing required fields — topic code is blank
        File missingField = File.createTempFile("csv_missing_field_", ".csv");
        missingField.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(missingField)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            pw.println(",Algorithms,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Monday,9:00,11:00,Building A,101");
        }

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        importer = new CSVImporter();
        List<ClassInstance> missingFieldResult = importer.importFromCSV(missingField.getAbsolutePath());

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();
        err = capErr.toString();

        assertTrue(missingFieldResult.isEmpty(),
                "CSVImporter must load no instances when a required field is blank");
        assertTrue(out.contains("SKIPPED"),
                "CSVImporter must print a SKIPPED message to System.out for a missing required field; got: " + out);
        assertTrue(err.isBlank(),
                "CSVImporter must not write to System.err for a missing required field; got: " + err);
    }

    @Test
    @Tag("Cameron")
    @Tag("Critical")
    @DisplayName("3.04: Verify that leaving all search criteria fields blank defaults to returning every class record stored in the application")
    void blankSearchCriteriaReturnsAllClassRecords() throws IOException {

        // Save streams for restore
        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;
        final InputStream realIn  = System.in;

        ByteArrayOutputStream capOut;
        ByteArrayOutputStream capErr;
        String out;

        // --- Load three distinct class instances via a well-formed CSV ---
        File csvFile = File.createTempFile("csv_search_test_", ".csv");
        csvFile.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(csvFile)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            pw.println("COMP1000,Programming 1,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Monday,9:00,11:00,Building A,101");
            pw.println("COMP2000,Data Structures,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Tuesday,10:00,12:00,Building B,202");
            pw.println("COMP3000,Algorithms,On Campus,City,Semester 1 2025,1," +
                    "Tutorial,1,3/03/2025,2/06/2025,Wednesday,13:00,14:00,Building C,303");
        }

        // Step 1: import the CSV via menu option 1
        // Step 2: invoke the search via menu option 4, pressing Enter for all 11 fields (blank criteria)
        // Step 3: exit via menu option 15
        //
        // The 11 blank lines correspond to the 11 readOptional() prompts in handleSearch():
        // topic code, topic name, attendance mode, campus, semester, class format,
        // day of week, building, room, start time, end time.
        String simulatedInput = "1\n"                      // menu: Import
                + csvFile.getAbsolutePath() + "\n"         // file path prompt
                + "4\n"                                    // menu: Search
                + "\n\n\n\n\n\n\n\n\n\n\n"                // 11 blank search fields
                + "15\n";                                  // menu: Exit

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Main.main(new String[]{});

        System.setOut(realOut);
        System.setErr(realErr);
        System.setIn(realIn);
        out = capOut.toString();

        // The search results section must report matches, not a "no matches" warning
        assertFalse(out.contains("No classes matched your search criteria"),
                "A blank search must not report zero matches when records are loaded; got: " + out);

        // All three topic codes must appear in the search results output
        assertTrue(out.contains("COMP1000"),
                "Blank search results must include COMP1000; got: " + out);
        assertTrue(out.contains("COMP2000"),
                "Blank search results must include COMP2000; got: " + out);
        assertTrue(out.contains("COMP3000"),
                "Blank search results must include COMP3000; got: " + out);

        // The match count must equal the total number of loaded instances (3)
        assertTrue(out.contains("Found 3 match"),
                "Blank search must report all 3 loaded class instances as matches; got: " + out);
    }

    @Test
    @Tag("Cameron")
    @Tag("Additional")
    @DisplayName("4.03: Verify that the system blocks generation if a user tries to mix Flinders City Campus classes with Bedford Park or Tonsley classes for the same topic")
    void generationIsBlockedWhenCampusFilterMixesCityWithOtherCampuses() {

        // Save streams for restore
        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;

        ByteArrayOutputStream capOut;
        ByteArrayOutputStream capErr;
        String out;

        TimetableGenerator generator = new TimetableGenerator();
        List<Topic> topics = List.of(new Topic("COMP1000", "Programming 1"));

        // Build one instance per campus for the same topic so each scenario is unambiguous
        ClassInstance cityInstance = buildClassInstance(
                "COMP1000", "Lecture", "City",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));

        ClassInstance bedfordInstance = buildClassInstance(
                "COMP1000", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));

        ClassInstance tonsleyInstance = buildClassInstance(
                "COMP1000", "Lecture", "Tonsley",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));

        // 1. City-only filter with only a Bedford Park instance → blocked
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        Timetable result1 = generator.generateTimetable(
                "T1", "Semester 1 2025",
                topics,
                List.of(bedfordInstance),
                new ArrayList<>(), false,
                List.of("City"));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNull(result1,
                "Generation must be blocked when a City-only filter is applied but only a Bedford Park instance exists for the topic");
        assertTrue(out.contains("[TimetableGenerator]"),
                "A blocking message must be printed to System.out; got: " + out);

        // 2. City-only filter with only a Tonsley instance → blocked
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        Timetable result2 = generator.generateTimetable(
                "T2", "Semester 1 2025",
                topics,
                List.of(tonsleyInstance),
                new ArrayList<>(), false,
                List.of("City"));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNull(result2,
                "Generation must be blocked when a City-only filter is applied but only a Tonsley instance exists for the topic");
        assertTrue(out.contains("[TimetableGenerator]"),
                "A blocking message must be printed to System.out; got: " + out);

        // 3. Bedford Park-only filter with only a City instance → blocked
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        Timetable result3 = generator.generateTimetable(
                "T3", "Semester 1 2025",
                topics,
                List.of(cityInstance),
                new ArrayList<>(), false,
                List.of("Bedford Park"));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNull(result3,
                "Generation must be blocked when a Bedford Park-only filter is applied but only a City instance exists for the topic");
        assertTrue(out.contains("[TimetableGenerator]"),
                "A blocking message must be printed to System.out; got: " + out);

        // 4. Tonsley-only filter with only a City instance → blocked
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        Timetable result4 = generator.generateTimetable(
                "T4", "Semester 1 2025",
                topics,
                List.of(cityInstance),
                new ArrayList<>(), false,
                List.of("Tonsley"));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNull(result4,
                "Generation must be blocked when a Tonsley-only filter is applied but only a City instance exists for the topic");
        assertTrue(out.contains("[TimetableGenerator]"),
                "A blocking message must be printed to System.out; got: " + out);
        //confirms the filter is working correctly
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        Timetable result5 = generator.generateTimetable(
                "T5", "Semester 1 2025",
                topics,
                List.of(cityInstance),
                new ArrayList<>(), false,
                List.of("City"));

        System.setOut(realOut);
        System.setErr(realErr);
        out = capOut.toString();

        assertNotNull(result5,
                "Generation must succeed when the campus filter matches the available instance; got output: " + out);
        assertTrue(out.contains("generated successfully"),
                "A success message must be printed to System.out when generation is not blocked; got: " + out);
    }
    @Test
    @Tag("Cameron")
    @Tag("Core")
    @DisplayName("5.01: Verify that the timetable view distinctly highlights structural time clashes or invalid campus commuting gaps to the user")
    void timetableViewHighlightsTimeClashesAndTravelGaps() throws IOException {

        // Save streams for restore
        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;
        final InputStream realIn  = System.in;

        ByteArrayOutputStream capOut;
        ByteArrayOutputStream capErr;
        String out;

        // ConflictDetector
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        ConflictDetector detector = new ConflictDetector();

        // Two instances on the same campus whose sessions overlap on Monday
        ClassInstance overlapA = buildClassInstance(
                "COMP1000", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(9, 0), LocalTime.of(11, 0));
        ClassInstance overlapB = buildClassInstance(
                "COMP2000", "Lecture", "Bedford Park",
                "Monday", LocalTime.of(10, 0), LocalTime.of(12, 0));

        List<ConflictDetector.ConflictPair> overlapPairs =
                detector.getConflicts(List.of(overlapA, overlapB));

        System.setOut(realOut);
        System.setErr(realErr);

        assertFalse(overlapPairs.isEmpty(),
                "Overlapping Monday sessions should produce at least one ConflictPair");
        assertTrue(overlapPairs.get(0).hasTimeOverlap(),
                "The conflict pair must be classified as TIME_OVERLAP");

        String overlapDescription = overlapPairs.get(0).getDetails().get(0).getDescription();
        assertTrue(overlapDescription.contains("Sessions overlap"),
                "TIME_OVERLAP description must contain 'Sessions overlap'; got: " + overlapDescription);
        assertTrue(overlapDescription.contains("Monday"),
                "TIME_OVERLAP description must name the day of the clash; got: " + overlapDescription);
        assertTrue(overlapDescription.contains("09:00"),
                "TIME_OVERLAP description must include the start time of the first session; got: " + overlapDescription);
        assertTrue(overlapDescription.contains("10:00"),
                "TIME_OVERLAP description must include the start time of the second session; got: " + overlapDescription);

        // 2. ConflictDetector: TRAVEL_TIME description contains the correct
        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));

        // Two instances on different campuses with only 10 minutes between sessions
        ClassInstance travelA = buildClassInstance(
                "COMP1000", "Tutorial", "City",
                "Tuesday", LocalTime.of(13, 0), LocalTime.of(14, 0));
        ClassInstance travelB = buildClassInstance(
                "COMP2000", "Tutorial", "Bedford Park",
                "Tuesday", LocalTime.of(14, 10), LocalTime.of(15, 0));

        List<ConflictDetector.ConflictPair> travelPairs =
                detector.getConflicts(List.of(travelA, travelB));

        System.setOut(realOut);
        System.setErr(realErr);

        assertFalse(travelPairs.isEmpty(),
                "A 10-min gap between City and Bedford Park should produce at least one ConflictPair");
        assertTrue(travelPairs.get(0).hasTravelConflict(),
                "The conflict pair must be classified as TRAVEL_TIME");

        String travelDescription = travelPairs.get(0).getDetails().get(0).getDescription();
        assertTrue(travelDescription.contains("Insufficient travel time"),
                "TRAVEL_TIME description must contain 'Insufficient travel time'; got: " + travelDescription);
        assertTrue(travelDescription.contains("Tuesday"),
                "TRAVEL_TIME description must name the day of the gap; got: " + travelDescription);
        assertTrue(travelDescription.contains("City"),
                "TRAVEL_TIME description must name the City campus; got: " + travelDescription);
        assertTrue(travelDescription.contains("Bedford Park"),
                "TRAVEL_TIME description must name the Bedford Park campus; got: " + travelDescription);
        assertTrue(travelDescription.contains("10 min"),
                "TRAVEL_TIME description must state the actual gap in minutes; got: " + travelDescription);
        assertTrue(travelDescription.contains("30 min"),
                "TRAVEL_TIME description must state the required minimum gap; got: " + travelDescription);

        // 3. Full Main integration
        File csvFile = File.createTempFile("csv_view_conflict_", ".csv");
        csvFile.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(csvFile)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room");
            // Two separate topics, both with a Monday 09:00-11:00 lecture on the same campus
            pw.println("COMP1000,Programming 1,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Monday,9:00,11:00,Building A,101");
            pw.println("COMP2000,Data Structures,On Campus,Bedford Park,Semester 1 2025,1," +
                    "Lecture,1,3/03/2025,2/06/2025,Monday,9:00,11:00,Building B,202");
        }

        String simulatedInput =
                "1\n"                           // menu: Import
                        + csvFile.getAbsolutePath() + "\n"
                        + "7\n"                         // menu: Select topics
                        + "1 2\n"                       // choose both topics
                        + "9\n"                         // menu: Generate timetable
                        + "MyTimetable\n"               // timetable name
                        + "Semester 1 2025\n"           // semester
                        + "y\n"                         // allow lecture overlaps → lets conflicting lectures through
                        + "\n"                          // campuses: blank = all
                        + "11\n"                        // menu: View timetable
                        + "1\n"                         // select timetable #1
                        + "15\n";                       // menu: Exit

        capOut = new ByteArrayOutputStream();
        capErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Main.main(new String[]{});

        System.setOut(realOut);
        System.setErr(realErr);
        System.setIn(realIn);
        out = capOut.toString();

        // The view must report how many conflicts were found
        assertTrue(out.contains("conflict(s) detected"),
                "Timetable view must print a conflict count when time clashes exist; got: " + out);

        // The view must print the '→' prefix that marks each individual conflict description
        assertTrue(out.contains("→"),
                "Timetable view must prefix each conflict description with '→'; got: " + out);

        // The time-overlap description must be present verbatim in the terminal output
        assertTrue(out.contains("Sessions overlap"),
                "Timetable view must display 'Sessions overlap' for a TIME_OVERLAP conflict; got: " + out);

        // The clean-bill-of-health message must NOT appear when conflicts exist
        assertFalse(out.contains("No scheduling conflicts detected"),
                "Timetable view must not claim no conflicts when a time clash is present; got: " + out);
    }

    @Test
    @Tag("Cameron")
    @Tag("Core")
    @DisplayName("6.02: Verify that deleting a saved timetable requires confirmation before removal from memory")
    void deletingTimetableRequiresConfirmationBeforeRemoval() throws IOException {

        final PrintStream realOut = System.out;
        final PrintStream realErr = System.err;
        final InputStream realIn  = System.in;

        ByteArrayOutputStream capOut = new ByteArrayOutputStream();
        ByteArrayOutputStream capErr = new ByteArrayOutputStream();

        // CSV containing one topic and one valid class instance
        File csvFile = File.createTempFile("csv_delete_tt_", ".csv");
        csvFile.deleteOnExit();

        try (PrintWriter pw = new PrintWriter(csvFile)) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number,"
                    + "Class Format,Instance Number,First Class Date,Last Class Date,"
                    + "Day of Week,Start Time,End Time,Building,Room");

            pw.println("COMP1000,Programming 1,On Campus,Bedford Park,Semester 1 2025,1,"
                    + "Lecture,1,3/03/2025,2/06/2025,Monday,9:00,11:00,Building A,101");
        }

        String simulatedInput =
                "1\n"                          // Import CSV
                        + csvFile.getAbsolutePath() + "\n"
                        + "7\n"                        // Select topics
                        + "1\n"
                        + "9\n"                        // Generate timetable
                        + "DeleteTest\n"
                        + "Semester 1 2025\n"
                        + "n\n"                        // do not allow overlaps
                        + "\n"                         // all campuses
                        + "13\n"                       // Delete timetable
                        + "1\n"                        // select DeleteTest
                        + "n\n"                        // CANCEL deletion
                        + "10\n"                       // Browse timetables
                        + "13\n"                       // Delete timetable again
                        + "1\n"                        // select DeleteTest
                        + "y\n"                        // CONFIRM deletion
                        + "10\n"                       // Browse timetables
                        + "15\n";                      // Exit

        System.setOut(new PrintStream(capOut));
        System.setErr(new PrintStream(capErr));
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Main.main(new String[]{});

        System.setOut(realOut);
        System.setErr(realErr);
        System.setIn(realIn);

        String out = capOut.toString();

        // Confirmation prompt must appear before deletion
        assertTrue(out.contains("Are you sure"),
                "Deleting a timetable must require a confirmation prompt; got: " + out);

        // First deletion attempt should be cancelled
        assertTrue(out.contains("Deletion cancelled"),
                "Rejecting the confirmation prompt must cancel deletion; got: " + out);

        // Timetable should still exist after cancellation
        assertTrue(out.contains("DeleteTest"),
                "Timetable should still be present after deletion is cancelled; got: " + out);

        // Second deletion attempt should succeed
        assertTrue(out.contains("deleted"),
                "Confirmed deletion must report that the timetable was deleted; got: " + out);

        // After deletion there should be no saved timetables
        assertTrue(out.contains("No timetables have been generated yet")
                        || out.contains("No timetables to delete"),
                "After confirmed deletion the timetable should no longer exist in memory; got: " + out);
    }

}
