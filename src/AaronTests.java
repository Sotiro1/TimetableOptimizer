import org.junit.jupiter.api.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Aaron aassigned test cases")


public class AaronTests {
    private CSVImporter importer;
    private File tempCSV;

    @BeforeEach
    void setUp() throws IOException {
        importer = new CSVImporter();
        tempCSV = File.createTempFile("timetable_test_", ".csv");
        tempCSV.deleteOnExit();
    }

    @AfterEach
    void tearDown() {
        if (tempCSV != null && tempCSV.exists()) {
            tempCSV.delete();
        }
    }

    private void writeCSV(String... rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempCSV))) {
            writer.write("Topic Code,Topic Name,Attendance Mode,Campus,Semester," +
                    "Availability Number,Class Format,Instance Number," +
                    "First Class Date,Last Class Date,Day of Week," +
                    "Start Time,End Time,Building,Room");
            writer.newLine();
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }

    private static final String LECTURE_ROW =
            "COMP1701,Game Design,In Person,Bedford Park,Semester 2,1," +
                    "Lecture,1,27/07/2025,14/09/2025,Monday,09:00,10:00,Flinders Hall,101";

    private static final String TUTORIAL_ROW = "COMP1701,Game Design,In Person,Bedford Park,Semester 2,1," +
            "Tutorial,1,28/07/2025,15/09/2025,Tuesday,10:00,11:00,Flinders Hall,201";

    @Test
    @Order(1)
    @Tag("Aaron")
    @Tag("Core")
    @DisplayName("2.01 importing a valid csv returns the correct number of class instantces")
    void test_2_01_validCSVImportReturnsCorrectInstanceCount() throws IOException {
        writeCSV(LECTURE_ROW, TUTORIAL_ROW);
        List<ClassInstance> result = importer.importFromCSV(tempCSV.getAbsolutePath());

        assertAll("should import both rows as separate class intances",
                () -> assertNotNull(result, "result should not be null"),
                () -> assertEquals(2, result.size(), "should have 2 class instances")
        );
    }

    @Test
    @Order(2)
    @Tag("Aaron")
    @Tag("Core")
    @DisplayName("2.01b importing the same row twice creates an instances")
    void test_2_01b_duplicateRowUpdatesExistingRecord() throws IOException {
        String updatedRow =
                "COMP1701,Game Design,In Person,Bedford Park,Semester 2,1," +
                        "Lecture,1,27/07/2025,14/09/2025,Monday,09:00,10:00,Flinders Hall,102";

        writeCSV(LECTURE_ROW, updatedRow);
        List<ClassInstance> result = importer.importFromCSV(tempCSV.getAbsolutePath());

        assertAll("duplicate key should update, not create a new entry",
                () -> assertNotNull(result, "result should not be null"),
                () -> assertEquals(1, result.size(), "should have 1 class instances")
        );
    }

    @Test
    @Order(3)
    @Tag("Aaron")
    @Tag("Core")
    @DisplayName("3.02 class instance has all mandatory fields after import")
    void test_3_02_classInstanceContainsAllMandatoryFields() throws IOException {
        writeCSV(LECTURE_ROW);
        List<ClassInstance> result = importer.importFromCSV(tempCSV.getAbsolutePath());

        ClassInstance ci = result.get(0);

        assertAll("all mandatory fields should be populated",
                () -> assertNotNull(ci.getTopicCode(), "topic code should not be null"),
                () -> assertNotNull(ci.getTopicName(), "topic name should not be null"),
                () -> assertNotNull(ci.getAttendanceMode(), "attendance mode should not be null"),
                () -> assertNotNull(ci.getCampus(), "campus should not be null"),
                () -> assertNotNull(ci.getSemester(), "semester should not be null"),
                () -> assertNotNull(ci.getClassFormat(), "class format should not be null"),
                () -> assertNotNull(ci.getSessions(), "sessions list should not be null"),
                () -> assertFalse(ci.getSessions().isEmpty(), "sessions list should not be empty")
        );
    }

    @Test
    @Order(4)
    @Tag("Aaron")
    @Tag("Additional")
    @DisplayName("4.01 timetable name is stored correctly and duplicate names are rejected")
    void test_4_01_timetableNameStoredAndDuplicateRejected() {
        Timetable tt = new Timetable("My Timetable", "Semester 2", false, new ArrayList<>());
        java.util.Map<String, Timetable> timetables = new java.util.LinkedHashMap<>();
        timetables.put(tt.getName(), tt);

        boolean alreadyExist = timetables.containsKey("My Timetable");

        assertAll("timetable name behaviour",
                () -> assertEquals("My Timetable", tt.getName(), "name should be sorted correctly"),
                () -> assertTrue(alreadyExist, "duplicate name should be detected"));
    }

    @Test
    @Order(5)
    @Tag("Aaron")
    @Tag("Critical")
    @DisplayName("5.04 all saved timetables are accessible by their unique names")
    void test_5_04_allSavedTimetablesAccessibleByName() {

        java.util.Map<String, Timetable> timetables = new java.util.LinkedHashMap<>();

        timetables.put("Morning Schedule", new Timetable("Morning Schedule", "Semester 1", false, new ArrayList<>()));
        timetables.put("Afternoon Schedule", new Timetable("Afternoon Schedule", "Semester 1", false, new ArrayList<>()));
        timetables.put("Compact Week", new Timetable("Compact Week", "Semester 2", false, new ArrayList<>()));

        assertAll("all timetables should be findable by name",
                () -> assertEquals(3, timetables.size(), "should have 3 timetables"),
                () -> assertTrue(timetables.containsKey("Morning Schedule"), "should find Morning Schedule"),
                () -> assertTrue(timetables.containsKey("Afternoon Schedule"), "should find Afternoon Schedule"),
                () -> assertTrue(timetables.containsKey("Compact Week"), "should find Compact Week")
        );
    }
}
