package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReverseWordTest {

    public String reverseWord(String word) {
        StringBuilder rev = new StringBuilder();
        for (int i = word.length() - 1; i >= 0; i--) rev.append(word.charAt(i));
        return rev.toString();
    }

    @Test
    public void testReverseWordNormal() {
        assertEquals("olleH", reverseWord("Hello"));
    }

    @Test
    public void testReverseSingleChar() {
        assertEquals("A", reverseWord("A"));
    }

    @Test
    public void testReverseWithSpaces() {
        assertEquals("dlroW olleH", reverseWord("Hello World"));
    }
}
