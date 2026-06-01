// John - testing if different campuses need 30 mins travel gap

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class Test405 {

    @Test
    @Tag("John")
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

}