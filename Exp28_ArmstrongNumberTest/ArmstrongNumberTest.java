package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArmstrongNumberTest {

    public boolean isArmstrong(int n) {
        int digits = 0;
        for (int t = n; t != 0; t /= 10) digits++;
        int sum = 0;
        for (int t = n; t != 0; t /= 10) {
            sum += (int) Math.pow(t % 10, digits);
        }
        return sum == n;
    }

    @Test
    public void testArmstrongTrue() {
        assertTrue(isArmstrong(153));
    }

    @Test
    public void testArmstrongFalse() {
        assertFalse(isArmstrong(123));
    }

    @Test
    public void testArmstrongSingleDigit() {
        assertTrue(isArmstrong(7));
    }
}
