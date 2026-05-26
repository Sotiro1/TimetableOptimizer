// John - testing if browsing classes groups same topic records together

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Test301 {

    @Test
    @Tag("John")
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

        // adding classes into one list
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

}