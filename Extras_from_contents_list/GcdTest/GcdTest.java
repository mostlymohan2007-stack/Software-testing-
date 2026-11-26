package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class GcdTest {

    public int gcd(int a, int b) {
        while (b != 0) {
            int r = a % b;
            a = b;
            b = r;
        }
        return Math.abs(a);
    }

    @Test
    public void testGcdNormal() {
        assertEquals(6, gcd(48, 18));
    }

    @Test
    public void testGcdCoprime() {
        assertEquals(1, gcd(17, 5));
    }

    @Test
    public void testGcdWithZero() {
        assertEquals(9, gcd(9, 0));
    }
}
