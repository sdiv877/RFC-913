package client;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

final class TestRunner {
    private static String CLIENT_WELCOME_MSG = "Successfully connected to localhost on port 6789";
    private static String SERVER_WELCOME_MSG = "+RFC 913 SFTP Server";

    private static List<TestOutcome> testResults = new ArrayList<TestOutcome>();

    public static void main(String[] argv) throws Exception {
        System.out.print("| RUNNING CLIENT TESTS |\n\n");

        testResults.add(test_User_id_valid());
        testResults.add(test_User_id_valid_account_required());
        testResults.add(test_User_id_valid_password_required());
        testResults.add(test_User_id_valid_account_and_password_required());
        testResults.add(test_User_id_valid_account_and_password_required_Multiple_accounts());

        System.out.println("| CLIENT TESTS COMPLETED |");
        printTestResults();
        waitForEnterKey();
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
                    System.out.println("Test " + (i + 1) + " failed.");
                    break;
                case Exception:
                    System.out.println("Test " + (i + 1) + " was not completed.");
                    break;
            }
        }
        System.out.println("PASSED: " + successCount + "/" + testResults.size() + " tests.");
    }

    private static void waitForEnterKey() {
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        scan.close();
    }

    private static void evalClientCommand(SFTPClient sftpClient, String cmd) throws Exception {
        System.out.println("> " + cmd);
        sftpClient.evalCommand(cmd);
    }

    private static TestOutcome test_User_id_valid() {
        System.out.println("1. User-id valid");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(3));
            testOutcome = (r1 && r2 && r3 && r4) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println("");
        return testOutcome;
    }

    private static TestOutcome test_User_id_valid_account_required() {
        System.out.println("2. User-id valid, account required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user2");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "acct acct1");
            r4 = assertEquals("!Account valid, logged-in", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println("");
        return testOutcome;
    }

    private static TestOutcome test_User_id_valid_password_required() {
        System.out.println("3. User-id valid, password required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user3");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "pass pass3");
            r4 = assertEquals("!Logged in", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println("");
        return testOutcome;
    }

    private static TestOutcome test_User_id_valid_account_and_password_required() {
        System.out.println("4. User-id valid, account and password required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "acct acct1");
            r4 = assertEquals("+Account valid, send password", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "pass pass4");
            r5 = assertEquals("!Logged in", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println("");
        return testOutcome;
    }

    private static TestOutcome test_User_id_valid_account_and_password_required_Multiple_accounts() {
        System.out.println("5. User-id valid, account and password required. Multiple accounts");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7, r8;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user5");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "pass pass5");
            r4 = assertEquals("+Send account", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("!Account valid, logged-in", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "acct acct2");
            r6 = assertEquals("!Account valid, logged-in", sftpClient.getServerResHistory().get(5));
            evalClientCommand(sftpClient, "acct acct3");
            r7 = assertEquals("!Account valid, logged-in", sftpClient.getServerResHistory().get(6));
            evalClientCommand(sftpClient, "done");
            r8 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(7));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7 && r8) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            System.out.println("| Test failed with exception |");
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println("");
        return testOutcome;
    }
}