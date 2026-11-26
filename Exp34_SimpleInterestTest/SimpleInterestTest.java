package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleInterestTest {

    public double calculateSimpleInterest(double principal, int time, boolean isSeniorCitizen) {
        double rate = isSeniorCitizen ? 12 : 10;
        return (principal * rate * time) / 100;
    }

    @Test
    public void testSeniorCitizenInterest() {
        assertEquals(1200.0, calculateSimpleInterest(10000, 1, true), 0.01);
    }

    @Test
    public void testNormalCustomerInterest() {
        assertEquals(1000.0, calculateSimpleInterest(10000, 1, false), 0.01);
    }

    @Test
    public void testSeniorCitizenTwoYears() {
        assertEquals(2400.0, calculateSimpleInterest(10000, 2, true), 0.01);
    }
}
