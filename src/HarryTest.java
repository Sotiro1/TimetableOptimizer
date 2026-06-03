import org.apache.logging.log4j.CloseableThreadContext;
import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TimeTableTest {

    private final InputStream originalSystemIn = System.in;

    @Test
    @DisplayName("2.02 Testing timetable update")
    @Tag("CART0404")
    @Tag("2.02 Core")
    void testTimeTableUpdate() throws IOException {

        File classes = new File("test_classes.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(classes))) {
            pw.println("Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number,Class Format,Instance Number,First Class Date,Last Class Date,Day of Week,Start Time,End Time,Building,Room");

            // First entry
            pw.println("COMP4567,Artificial Intelligence,In Person,City West,1,2,Tutorial,4,01/03/2025,01/06/2025,Thursday,10:00,11:00,City West Campus,CW207");

            // Second entry: Identical identification keys, but updated date/time/room details
            pw.println("COMP4567,Artificial Intelligence,In Person,City West,1,2,Tutorial,4,01/03/2025,01/06/2025,Thursday,9:00,10:00,City West Campus,CW207");
        }

        CSVImporter importer = new CSVImporter();
        List<ClassInstance> classInstanceList = importer.importFromCSV(classes.getAbsolutePath());

        assertEquals(1, classInstanceList.size(), "Identical class with a different time/date should overwrite previous instance.");
    }

    void writeToConsole() {

    }

    @Test
    @DisplayName("3.03 Search Criteria Results")
    @Tag("CART0404")
    @Tag("3.03 Additional")
    void searchCriteriaTest() throws IOException {

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        String combinedInput = "1\nclasses.csv\n4\nCOMP1234\n\n\n\n\nLecture\n\n\n\n\n\n15\n";

        System.setIn(new ByteArrayInputStream(combinedInput.getBytes()));

        Main.main(new String[0]);

        String capturedOutput = outputStreamCaptor.toString().trim();
        assertTrue(capturedOutput.contains("COMP1234") && capturedOutput.contains("Lecture"));

        System.setOut(originalOut);
        System.setIn(originalIn);



    }

    @Test
    @DisplayName("4.02 Zero topic time table")
    @Tag("CART0404")
    @Tag("4.02 Critical")
    void zeroTopicTest() throws IOException {

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));


        String combinedInput = "1\nclasses.csv\n9\n15\n";
        System.setIn(new ByteArrayInputStream(combinedInput.getBytes()));


        Main.main(new String[0]);


        String capturedOutput = outputStreamCaptor.toString().trim();
        assertTrue(capturedOutput.contains("No topics selected yet"));


        System.setOut(originalOut);
        System.setIn(originalIn);

    }

    @Test
    @DisplayName("4.07 Customization Test")
    @Tag("CART0404")
    @Tag("4.07 Additional")
    void CustomizationTest() throws IOException {

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));


        String combinedInput = "1\nclasses.csv\n8\n1\nTonsley\n1\n8\n8\n8\n15\n";
        System.setIn(new ByteArrayInputStream(combinedInput.getBytes()));


        Main.main(new String[0]);


        String capturedOutput = outputStreamCaptor.toString().trim();
        assertTrue(capturedOutput.contains("1. [Priority 1] PREFER_CAMPUS:Tonsley"));


        System.setOut(originalOut);
        System.setIn(originalIn);

    }

}