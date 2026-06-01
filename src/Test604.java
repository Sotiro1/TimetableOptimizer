// John - testing if exit option closes application safely

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Test604 {

    @Test
    @Tag("John")
    @Tag("Additional")
    @DisplayName("6.04 Verify exit option closes application safely")
    void testExitApplication() {

        // simulating user selecting exit option
        String input = "15\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // checking app exits without crashing
        assertDoesNotThrow(() -> {

            Main.main(new String[0]);

        });

    }

}