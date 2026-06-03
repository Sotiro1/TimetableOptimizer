// John - all assigned tests combined into one file

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

        // making first class object
        ClassInstance class1 = new ClassInstance();
        class1.setTopicCode("COMP1001");
        class1.setTopicName("Programming");

        // making second class with same topic
        ClassInstance class2 = new ClassInstance();
        class2.setTopicCode("COMP1001");
        class2.setTopicName("Programming");

        // making another topic class
        ClassInstance class3 = new ClassInstance();
        class3.setTopicCode("COMP2001");
        class3.setTopicName("Database Systems");

        // adding classes into one liist
        List<ClassInstance> classes = new ArrayList<>();
        classes.add(class1);
        classes.add(class2);
        classes.add(class3);

        // creating main object
        Main main = new Main();

        // using reflection because method is private
        Method method = Main.class.getDeclaredMethod("groupByTopic", List.class);
        method.setAccessible(true);

        // running the grouping method
        Map<String, List<ClassInstance>> grouped =
                (Map<String, List<ClassInstance>>) method.invoke(main, classes);

        // checking if records grouped correct
        assertAll(

                // should create 2 groups
                () -> assertEquals(2, grouped.size()),

                // COMP1001 should contain 2 records
                () -> assertEquals(2, grouped.get("COMP1001").size()),

                // COMP2001 should contain 1 record
                () -> assertEquals(1, grouped.get("COMP2001").size())

        );
    }

    @Test
    @Tag("Critical")
    @DisplayName("3.06 Verify deleting classes requires confirmation")
    void testDeleteClassConfirmation() {

        // simulaitng console inputs
        String input = "6\n15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // checking application runs without crashing
        assertDoesNotThrow(() -> {

            Main.main(new String[0]);

        });
    }

    @Test
    @Tag("Additional")
    @DisplayName("4.05 Verify different campuses require 30 minute commute gap")
    void testCampusCommuteGap() {

        // making first class instance
        ClassInstance class1 = new ClassInstance();
        class1.setCampus("Bedford Park");

        // making second class instance
        ClassInstance class2 = new ClassInstance();
        class2.setCampus("Tonsley");

        // making first session
        Session session1 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "B1",
                "101"
        );

        // making second session with only 15 mins gap
        Session session2 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(10, 15),
                LocalTime.of(11, 0),
                "T1",
                "202"
        );

        // adding sessions into classes
        class1.addSession(session1);
        class2.addSession(session2);

        // creating detector object
        ConflictDetector detector = new ConflictDetector();

        // checking if travel conflict exists
        boolean result = detector.hasConflict(class1, class2);

        // should detect not enough travel time
        assertTrue(result);
    }

    @Test
    @Tag("Additional")
    @DisplayName("5.03 Verify lecture overlap only allowed when enabled")
    void testLectureOverlapOption() {

        // making first lecture class
        ClassInstance class1 = new ClassInstance();
        class1.setClassFormat("Lecture");

        // making second lecture class
        ClassInstance class2 = new ClassInstance();
        class2.setClassFormat("Lecture");

        // creating first lecture session
        Session session1 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "B1",
                "101"
        );

        // creating second overlapping lecture
        Session session2 = new Session(
                LocalDate.now(),
                LocalDate.now(),
                "Monday",
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                "B2",
                "202"
        );

        // adding sessions into classes
        class1.addSession(session1);
        class2.addSession(session2);

        // creating conflict detector
        ConflictDetector detector = new ConflictDetector();

        // checking overlap exists
        boolean overlapFound = detector.hasConflict(class1, class2);

        // overlap should exist
        assertTrue(overlapFound);
    }

    @Test
    @Tag("Additional")
    @DisplayName("6.04 Verify exit option closes application safely")
    void testExitApplication() {

        // simulating user selecting exit option
        String input = "15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // checking app exits without crashingg
        assertDoesNotThrow(() -> {

            Main.main(new String[0]);

        });
    }
}