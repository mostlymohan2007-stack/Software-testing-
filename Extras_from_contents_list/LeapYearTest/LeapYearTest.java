package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class LeapYearTest {

    public boolean isLeapYear(int year) {
        return year % 400 == 0 || (year % 4 == 0 && year % 100 != 0);
    }

    @Test
    public void testLeapYear() {
        assertTrue(isLeapYear(2024));
    }

    @Test
    public void testCenturyNotLeap() {
        assertFalse(isLeapYear(1900));
    }

    @Test
    public void testFourHundredRule() {
        assertTrue(isLeapYear(2000));
    }
}
