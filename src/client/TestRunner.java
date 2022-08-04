package client;

import java.util.List;
import java.util.ArrayList;

final class TestRunner {
    private static String CLIENT_WELCOME_MSG = "Successfully connected to localhost on port 6789";
    private static String SERVER_WELCOME_MSG = "+RFC 913 SFTP Server";

    private static List<TestOutcome> testResults = new ArrayList<TestOutcome>();

    public static void main(String[] argv) {
        System.out.print("| RUNNING CLIENT TESTS |\n\n");

        testResults.add(test_User_id_valid());

        printTestResults();

        System.out.println("| CLIENT TESTS COMPLETED |");
    }

    private static TestOutcome test_User_id_valid() {
        System.out.println("1. User-id valid");

        SFTPClient sftpClient = new SFTPClient();
        boolean succesful = false;
        TestOutcome testOutcome;

        succesful = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        succesful = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));

        try {
            sftpClient.evalCommand("user user1");
            succesful = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            sftpClient.evalCommand("done");
            succesful = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(3));
            testOutcome = succesful ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        sftpClient.closeConnection();
        return testOutcome;
    }

    private static boolean assertEquals(String expected, String actual) {
        return actual.equals(expected);
    }

    private static void printTestResults() {
        int successCount = 0;
        for (int i = 0; i < testResults.size(); i++) {
            TestOutcome outcome = testResults.get(i);
            switch (outcome) {
                case Success:
                    successCount++;
                    break;
                case Failure:
                    System.out.println("Test " + i + " failed");
                    break;
                case Exception:
                    System.out.println("Test " + i + " was not completed");
                    break;
            }
        }
        System.out.println("\nPASSED: " + successCount + "/" + testResults.size() + " tests.");
    }
}