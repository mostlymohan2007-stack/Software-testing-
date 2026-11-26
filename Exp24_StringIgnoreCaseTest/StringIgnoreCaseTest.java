package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringIgnoreCaseTest {

    public boolean equalsIgnoringCase(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return s1.equalsIgnoreCase(s2);
    }

    @Test
    public void testStringEqualIgnoringCase() {
        assertTrue(equalsIgnoringCase("Java", "java"));
    }

    @Test
    public void testStringNotEqual() {
        assertFalse(equalsIgnoringCase("Java", "Python"));
    }

    @Test
    public void testNullString() {
        assertFalse(equalsIgnoringCase(null, "test"));
    }
}
