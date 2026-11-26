package junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class VotingSystemTest {

    public String checkEligibility(int age) {
        return (age >= 18) ? "Eligible" : "Not Eligible";
    }

    @Test
    public void testEligible() {
        assertEquals("Eligible", checkEligibility(20));
    }

    @Test
    public void testNotEligible() {
        assertEquals("Not Eligible", checkEligibility(16));
    }

    @Test
    public void testBoundaryAge() {
        assertEquals("Eligible", checkEligibility(18));
    }
}
