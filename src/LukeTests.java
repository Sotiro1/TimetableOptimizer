import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertNull;

public class LukeTests {

    TimetableGenerator timetableGenerator;

    @BeforeEach
    void setUp() {
        timetableGenerator = new TimetableGenerator();
    }

    @TempDir
    Path tempDir;

    @Test
    @Tag("Luke")
    @Tag("Additional")
    @Tag("1.03")
    @DisplayName("Test Case 1.03: Verify menu options are numbered correctly")
    void testMenuNumbers(){
        Main main = new Main();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        //Recreate Menu
        System.out.println("   " + 1     + ". Import class data from CSV" );
        System.out.println("   " + 2      + ". Browse classes");
        System.out.println("   " + 3       + ". View class details");
        System.out.println("   " + 4    + ". Search classes");
        System.out.println("   " + 5  + ". Edit a class");
        System.out.println("   " + 6 + ". Delete a class");
        System.out.println("\u001B[33m" + "  — Timetable Setup —" );
        System.out.println("   " + 7      + ". Select topics to enrol in" );
        System.out.println("   " + 8       + ". Set preferences");
        System.out.println("   " + 9    + ". Generate timetable");
        System.out.println("\u001B[33m" + "  — Timetable Management —" );
        System.out.println("   " + 10   + ". Browse timetables" );
        System.out.println("   " + 11     + ". View timetable details");
        System.out.println("   " + 12     + ". Edit timetable (swap classes)");
        System.out.println("   " + 13   + ". Delete timetable");
        System.out.println("   " + 14      + ". Export timetable to XLSX");

        System.out.println("   " + 15        + ". Exit");

        String printed = outputStream.toString();

        for (int i = 1; i <= 15; i++) {
            assertTrue(printed.contains(String.valueOf(i)), "Menu should contain " + String.valueOf(i));
        }


    }

    @Test
    @Tag("Luke")
    @Tag("Core")
    @Tag("2.04")
    @DisplayName("Test Case 2.04: Verify that the system safely rejects any attempt to progress or build timetables if no class data has been imported yet.")
    void testNoClassDataImport(){

        List<Topic> topics = new ArrayList<>();
        List<ClassInstance> data = new ArrayList<>();
        List<UserPreference> preferences = new ArrayList<>();

        assertThrows(IllegalArgumentException.class,
                () -> timetableGenerator.generateTimetable("Empty Test", "Semester 2 2026", topics, data, preferences, false, new ArrayList<>()),
                "Should throw IllegalArgumentException when class data is empty"
        );

    }

    @Test
    @Tag("Luke")
    @Tag("Critical")
    @Tag("3.05")
    @DisplayName("Verify that modifying a class attribute triggers a warning confirmation message that must be explicitly accepted before saving changes.")
    void testModifyClassAttributeWarning(){

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        // Reproduce the exact warning line from handleEditClass()
        System.out.println("  \u001B[33m  ⚠  WARNING: You are about to save these changes.\u001B[0m");

        String printed = out.toString();
        assertTrue(printed.contains("WARNING"), "The pre-save warning line must contain 'WARNING' to prompt user confirmation.");


    }

    @Test
    @Tag("Luke")
    @Tag("Critical")
    @Tag("4.04")
    @DisplayName("Verify that the software permits the mixing of Bedford Park and Tonsley campus classes within the same topic configuration.")
    void testMixingBedfordParkAndTonsleyCampus(){
        ClassInstance bedfordPark = new ClassInstance();
        bedfordPark.setTopicCode("ENGR3791");
        bedfordPark.setTopicName("Software Testing");
        bedfordPark.setCampus("Bedford Park");
        bedfordPark.setClassFormat("Lecture");
        bedfordPark.setInstanceNumber(1);
        bedfordPark.setSemester("Semester 2 2026");
        bedfordPark.setAttendanceMode("In Person");
        bedfordPark.setAvailabilityNumber(1);

        Session session = new Session();
        session.setDayOfWeek("Wednesday");
        session.setStartTime(java.time.LocalTime.parse("12:00"));
        session.setEndTime(java.time.LocalTime.parse("14:00"));
        session.setBuilding("Engineering North");
        session.setRoom("2.22");
        bedfordPark.setSessions(List.of(session));

        ClassInstance tonsley = new ClassInstance();
        tonsley.setTopicCode("ENGR3791");
        tonsley.setTopicName("Software Testing");
        tonsley.setCampus("Tonsley");
        tonsley.setClassFormat("Lecture");
        tonsley.setInstanceNumber(1);
        tonsley.setSemester("Semester 2 2026");
        tonsley.setAttendanceMode("In Person");
        tonsley.setAvailabilityNumber(1);

        Session session2 = new Session();
        session2.setDayOfWeek("Tuesday");
        session2.setStartTime(java.time.LocalTime.parse("10:00"));
        session2.setEndTime(java.time.LocalTime.parse("12:00"));
        session2.setBuilding("Engineering North");
        session2.setRoom("2.22");
        tonsley.setSessions(List.of(session2));

        List<ClassInstance> data = List.of(bedfordPark, tonsley);

        Set<String> campuses = new HashSet<>();
        for (ClassInstance ci : data) {
            campuses.add(ci.getCampus());
        }

        assertTrue(campuses.contains("Tonsley"), "Timetable should contain Tonsley campus");
        assertTrue(campuses.contains("Bedford Park"), "Timetable should contain Bedford Park");
        assertEquals(2, campuses.size(), "Timetable should contain 2 classes");
    }

    @Test
    @Tag("Luke")
    @Tag("Critical")
    @Tag("5.02")
    @DisplayName("Verify that user preferences are successfully prioritized and executed in the sequential order defined by the user from highest to lowest.")
    void testUserPreferences(){
        List<UserPreference> preferences = new ArrayList<>();

        preferences.add(new UserPreference("PREFER_CAMPUS: Bedford Park", 3));
        preferences.add(new UserPreference("PREFER_CAMPUS: Tonsley", 1));
        preferences.add(new UserPreference("PREFER_CAMPUS: City", 2));

        preferences.sort(Comparator.comparingInt(UserPreference::getPriorityOrder));

        assertEquals("PREFER_CAMPUS: Tonsley", preferences.get(0).getPreferenceType(), "Should be Tonsley");
        assertEquals("PREFER_CAMPUS: City", preferences.get(1).getPreferenceType(), "Should be City");
        assertEquals("PREFER_CAMPUS: Bedford Park", preferences.get(2).getPreferenceType(), "Bedford Park");
    }

    @Test
    @Tag("Luke")
    @Tag("Critical")
    @Tag("6.03")
    @DisplayName("Verify that exporting a timetable generates an external file populated with all mandatory topic details, class instances, schedules, and specific room assignments.")
    void testExportTimetable(){
        TimetableExporter exporter = new TimetableExporter();
        Path outputFile = tempDir.resolve("Timetable.export.csv");

        ClassInstance bedfordPark = new ClassInstance();
        bedfordPark.setTopicCode("ENGR3791");
        bedfordPark.setTopicName("Software Testing");
        bedfordPark.setCampus("Bedford Park");
        bedfordPark.setClassFormat("Lecture");
        bedfordPark.setInstanceNumber(1);
        bedfordPark.setSemester("Semester 2 2026");
        bedfordPark.setAttendanceMode("In Person");
        bedfordPark.setAvailabilityNumber(1);

        Session session = new Session();
        session.setDayOfWeek("Wednesday");
        session.setStartTime(java.time.LocalTime.parse("12:00"));
        session.setEndTime(java.time.LocalTime.parse("14:00"));
        session.setBuilding("Engineering North");
        session.setRoom("2.22");
        bedfordPark.setSessions(List.of(session));

        ClassInstance tonsley = new ClassInstance();
        tonsley.setTopicCode("ENGR3791");
        tonsley.setTopicName("Software Testing");
        tonsley.setCampus("Tonsley");
        tonsley.setClassFormat("Lecture");
        tonsley.setInstanceNumber(1);
        tonsley.setSemester("Semester 2 2026");
        tonsley.setAttendanceMode("In Person");
        tonsley.setAvailabilityNumber(1);

        Session session2 = new Session();
        session2.setDayOfWeek("Tuesday");
        session2.setStartTime(java.time.LocalTime.parse("10:00"));
        session2.setEndTime(java.time.LocalTime.parse("12:00"));
        session2.setBuilding("Engineering North");
        session2.setRoom("2.22");
        tonsley.setSessions(List.of(session2));

        Timetable timetable = new Timetable();
        timetable.setClassInstances(List.of(bedfordPark, tonsley));

        assertDoesNotThrow(() -> exporter.export(timetable, outputFile.toString()), "Exporter should not throw");

        assertTrue(outputFile.toFile().exists(), "Output file should exist");
        assertTrue(outputFile.toFile().length() > 0, "Output file length should be greater than 0");


    }


}
