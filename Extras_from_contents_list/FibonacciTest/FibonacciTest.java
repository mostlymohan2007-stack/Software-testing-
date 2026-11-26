package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class FibonacciTest {

    public long fibonacci(int n) {
        if (n < 1) throw new IllegalArgumentException("n must be 1 or more");
        long a = 0, b = 1;
        for (int i = 1; i < n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return a;
    }

    @Test
    public void testFirstTerm() {
        assertEquals(0L, fibonacci(1));
    }

    @Test
    public void testSeventhTerm() {
        assertEquals(8L, fibonacci(7));
    }

    @Test
    public void testTenthTerm() {
        assertEquals(34L, fibonacci(10));
    }
}
