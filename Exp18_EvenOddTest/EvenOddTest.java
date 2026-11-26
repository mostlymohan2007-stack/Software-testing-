package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class EvenOddTest {

    public String checkEvenOdd(int n) {
        return (n % 2 == 0) ? "Even" : "Odd";
    }

    @Test
    public void testEvenNumber() {
        assertEquals("Even", checkEvenOdd(10));
    }

    @Test
    public void testOddNumber() {
        assertEquals("Odd", checkEvenOdd(15));
    }

    @Test
    public void testZero() {
        assertEquals("Even", checkEvenOdd(0));
    }
}
