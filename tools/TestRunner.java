import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.Request;

/** Runs one JUnit 4 test class and prints the status of every test method. */
public class TestRunner {
    public static void main(String[] args) throws Exception {
        JUnitCore core = new JUnitCore();
        core.addListener(new RunListener() {
            private boolean bad;
            @Override public void testStarted(Description d) { bad = false; }
            @Override public void testFailure(Failure f) {
                bad = true;
                System.out.println("  FAILED  " + f.getDescription().getMethodName() + " : " + f.getMessage());
            }
            @Override public void testAssumptionFailure(Failure f) {
                bad = true;
                System.out.println("  SKIPPED " + f.getDescription().getMethodName() + " : " + f.getMessage());
            }
            @Override public void testFinished(Description d) {
                if (!bad) System.out.println("  PASSED  " + d.getMethodName());
            }
        });
        Class<?> test = Class.forName(args[0]);
        System.out.println("Running " + args[0]);
        Result r = core.run(test);
        System.out.printf("%nTests run: %d, Failures: %d, Time: %.3f s%n",
                r.getRunCount(), r.getFailureCount(), r.getRunTime() / 1000.0);
        String verdict = !r.wasSuccessful() ? "FAILURE" : r.getAssumptionFailureCount() > 0 ? "SKIPPED" : "SUCCESS";
        System.out.println("RESULT: " + verdict);
        System.exit(r.wasSuccessful() ? 0 : 1);
    }
}
