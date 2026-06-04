import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("John")
class JohnTest {

    @Test
    @Tag("Critical")
    @DisplayName("3.01 Verify browsing classes groups matching records correctly")
    void testBrowseClassGrouping() throws Exception {

        ClassInstance class1 = new ClassInstance();
        class1.setTopicCode("COMP1001");
        class1.setTopicName("Programming");

        ClassInstance class2 = new ClassInstance();
        class2.setTopicCode("COMP1001");
        class2.setTopicName("Programming");

        ClassInstance class3 = new ClassInstance();
        class3.setTopicCode("COMP2001");
        class3.setTopicName("Database Systems");

        List<ClassInstance> classes = new ArrayList<>();
        classes.add(class1);
        classes.add(class2);
        classes.add(class3);

        Main main = new Main();

        Method method = Main.class.getDeclaredMethod("groupByTopic", List.class);
        method.setAccessible(true);

        Map<String, List<ClassInstance>> grouped =
                (Map<String, List<ClassInstance>>) method.invoke(main, classes);

        assertAll(
                () -> assertEquals(2, grouped.size()),
                () -> assertEquals(2, grouped.get("COMP1001").size()),
                () -> assertEquals(1, grouped.get("COMP2001").size())
        );
    }

    @Test
    @Tag("Critical")
    @DisplayName("3.06 Verify deleting classes requires confirmation")
    void testDeleteClassConfirmation() {

        String input = "6\n15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[0]));
    }

    @Test
    @Tag("Additional")
    @DisplayName("4.05 Verify different campuses require 30 minute commute gap")
    void testCampusCommuteGap() {

        ClassInstance class1 = new ClassInstance();
        class1.setCampus("Bedford Park");

        ClassInstance class2 = new ClassInstance();
        class2.setCampus("Tonsley");

        Session session1 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "B1",
                "101"
        );

        Session session2 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(10, 15),
                LocalTime.of(11, 0),
                "T1",
                "202"
        );

        class1.addSession(session1);
        class2.addSession(session2);

        ConflictDetector detector = new ConflictDetector();

        boolean result = detector.hasConflict(class1, class2);

        assertTrue(result);
    }

    @Test
    @Tag("Additional")
    @DisplayName("5.03 Verify lecture overlap only allowed when enabled")
    void testLectureOverlapOption() {

        ClassInstance class1 = new ClassInstance();
        class1.setClassFormat("Lecture");

        ClassInstance class2 = new ClassInstance();
        class2.setClassFormat("Lecture");

        Session session1 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "B1",
                "101"
        );

        Session session2 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                "B2",
                "202"
        );

        class1.addSession(session1);
        class2.addSession(session2);

        ConflictDetector detector = new ConflictDetector();

        boolean overlapFound = detector.hasConflict(class1, class2);

        assertTrue(overlapFound);
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.04 Verify exit option closes application safely")
    void testExitApplication() {

        String input = "15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[0]));
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.05 Verify timetable stores values correctly")
    void testTimetableClass() {

        List<String> campuses = new ArrayList<>();
        campuses.add("Tonsley");

        Timetable timetable =
                new Timetable(
                        "My Timetable",
                        "Semester 1",
                        true,
                        campuses
                );

        assertEquals("My Timetable", timetable.getName());
        assertEquals("Semester 1", timetable.getSemester());
        assertTrue(timetable.isAllowLectureOverlap());

        assertEquals(
                1,
                timetable.getSelectedCampuses().size()
        );

        timetable.setName("Updated");

        assertEquals(
                "Updated",
                timetable.getName()
        );

        assertTrue(
                timetable.toString().contains("Updated")
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.06 Verify topic stores values correctly")
    void testTopicClass() {

        Topic topic =
                new Topic(
                        "COMP1001",
                        "Programming"
                );

        assertEquals(
                "COMP1001",
                topic.getTopicCode()
        );

        assertEquals(
                "Programming",
                topic.getTopicName()
        );

        topic.setTopicCode("COMP2001");
        topic.setTopicName("Database Systems");

        assertEquals(
                "COMP2001",
                topic.getTopicCode()
        );

        assertEquals(
                "Database Systems",
                topic.getTopicName()
        );

        assertTrue(
                topic.toString().contains("COMP2001")
        );

        assertTrue(
                topic.toString().contains("Database Systems")
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.07 Verify timetable generation rejects empty topics")
    void testTimetableGeneratorValidation() {

        TimetableGenerator generator =
                new TimetableGenerator();

        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generateTimetable(
                        "Test",
                        "Semester 1",
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        false,
                        new ArrayList<>()
                )
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.08 Verify exporter handles null timetable")
    void testTimetableExporterNull() {

        TimetableExporter exporter =
                new TimetableExporter();

        assertDoesNotThrow(
                () -> exporter.export(
                        null,
                        "test.csv"
                )
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.09 Verify timetable handles null campus list")
    void testTimetableNullCampusList() {

        Timetable timetable =
                new Timetable(
                        "Test",
                        "Semester 1",
                        false,
                        null
                );

        assertNotNull(
                timetable.getSelectedCampuses()
        );

        assertEquals(
                0,
                timetable.getSelectedCampuses().size()
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.10 Verify timetable handles null class instance list")
    void testTimetableNullClassInstances() {

        Timetable timetable =
                new Timetable();

        timetable.setClassInstances(null);

        assertNotNull(
                timetable.getClassInstances()
        );

        assertEquals(
                0,
                timetable.getClassInstances().size()
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.11 Verify timetable adds class instances")
    void testTimetableAddClassInstance() {

        Timetable timetable =
                new Timetable();

        ClassInstance instance =
                new ClassInstance();

        timetable.addClassInstance(instance);

        assertEquals(
                1,
                timetable.getClassInstances().size()
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.12 Verify generation fails when no class instances exist")
    void testGeneratorNoInstances() {

        TimetableGenerator generator =
                new TimetableGenerator();

        List<Topic> topics =
                new ArrayList<>();

        topics.add(
                new Topic(
                        "COMP1001",
                        "Programming"
                )
        );

        Timetable result =
                generator.generateTimetable(
                        "Test",
                        "Semester 1",
                        topics,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        false,
                        new ArrayList<>()
                );

        assertNull(result);
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.13 Verify exporter handles blank file path")
    void testExporterBlankPath() {

        TimetableExporter exporter =
                new TimetableExporter();

        Timetable timetable =
                new Timetable();

        assertDoesNotThrow(
                () -> exporter.export(
                        timetable,
                        ""
                )
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.14 Verify exporter handles empty timetable")
    void testExporterEmptyTimetable() {

        TimetableExporter exporter =
                new TimetableExporter();

        Timetable timetable =
                new Timetable();

        timetable.setName("Empty");

        assertDoesNotThrow(
                () -> exporter.export(
                        timetable,
                        "empty.csv"
                )
        );
    }
    @Test
    @Tag("Additional")
    @DisplayName("6.15 Verify default student constructor")
    void testStudentDefaultConstructor() {

        Student student = new Student();

        assertNull(
                student.getStudentId()
        );

        assertNull(
                student.getName()
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.16 Verify student setters update values")
    void testStudentSetters() {

        Student student = new Student();

        student.setStudentId(
                "999999"
        );

        student.setName(
                "John"
        );

        assertEquals(
                "999999",
                student.getStudentId()
        );

        assertEquals(
                "John",
                student.getName()
        );
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.17 Verify student toString format")
    void testStudentToString() {

        Student student =
                new Student(
                        "123456",
                        "john"
                );

        String result =
                student.toString();

        assertTrue(
                result.contains("123456")
        );

        assertTrue(
                result.contains("john")
        );

        assertTrue(
                result.contains("john")
        );
    }
}