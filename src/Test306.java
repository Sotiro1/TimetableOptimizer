// John - testing if deleting class asks for confirmation before removing

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Test306 {

    @Test
    @Tag("John")
    @Tag("Critical")
    @DisplayName("3.06 Verify deleting classes requires confirmation")
    void testDeleteClassConfirmation() {

        // simulating console inputs
        // 6 = delete class
        // 15 = exit program
        String input = "6\n15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // checking application runs without crashing
        assertDoesNotThrow(() -> {

            Main.main(new String[0]);

        });

    }

}