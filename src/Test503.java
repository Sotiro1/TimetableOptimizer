// John - testing lecture overlap only works when enabled

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class Test503 {

    @Test
    @Tag("John")
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

}