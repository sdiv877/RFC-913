package test;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import client.SFTPClient;
import utils.Utils;

final class TestRunner {
    private static String CLIENT_WELCOME_MSG = "Successfully connected to localhost on port 6789";
    private static String SERVER_WELCOME_MSG = "+RFC 913 SFTP Server";
    private static String ANY_NEWLINE = "\r?\n|\r";

    private static List<TestOutcome> testResults = new ArrayList<TestOutcome>();

    public static void main(String[] argv) throws Exception {
        System.out.println("| RUNNING CLIENT TESTS |\n");

        testResults.add(test_User_id_valid());
        testResults.add(test_User_id_valid_account_required());
        testResults.add(test_User_id_valid_password_required());
        testResults.add(test_User_id_valid_account_and_password_required());
        testResults.add(test_User_id_valid_account_and_password_required_Multiple_accounts());
        testResults.add(test_User_id_invalid());
        testResults.add(test_Account_invalid());
        testResults.add(test_Password_invalid());
        testResults.add(test_User_id_argument_error());
        testResults.add(test_Account_argument_error());
        testResults.add(test_Password_argument_error());
        testResults.add(test_Type());
        testResults.add(test_Type_argument_error());
        testResults.add(test_List_standard_current_directory());
        testResults.add(test_List_standard_other_directory());
        testResults.add(test_List_verbose_current_directory());
        testResults.add(test_List_non_existent_directory());
        testResults.add(test_List_file_instead_of_directory());
        testResults.add(test_List_argument_error());
        testResults.add(test_Change_directory_relative_path());
        testResults.add(test_Change_directory_user_path());
        testResults.add(test_Change_directory_absolute_path());
        testResults.add(test_Change_directory_non_existent_directory());
        testResults.add(test_Change_directory_file_instead_of_directory());
        testResults.add(test_Change_directory_account_required());
        testResults.add(test_Change_directory_password_required());
        testResults.add(test_Change_directory_account_and_password_required());
        testResults.add(test_Change_directory_argument_error());

        System.out.println("| CLIENT TESTS COMPLETED |");
        printTestResults();
        Utils.waitForEnterKey();
    }

    private static boolean assertEquals(String expected, String actual) {
        return actual.equals(expected);
    }

    private static boolean assertContainsAll(List<String> expectedList, String actual, String regex) {
        ArrayList<String> actualList = Utils.splitString(actual, regex);
        for (String expected : expectedList) {
            if (!actualList.contains(expected)) {
                System.out.println("MISSING: " + expected);
                return false;
            }
        }
        return true;
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
        System.out.println("Test 16 requires manual verification.");
        System.out.println("PASSED: " + successCount + "/" + testResults.size() + " tests.");
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
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
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
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
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
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
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
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
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
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_User_id_invalid() {
        System.out.println("6. User-id invalid");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user7");
            r3 = assertEquals("-Invalid user-id, try again", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(3));
            testOutcome = (r1 && r2 && r3 && r4) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Account_invalid() {
        System.out.println("7. Account invalid");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "acct acct2");
            r4 = assertEquals("-Invalid account, try again", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Password_invalid() {
        System.out.println("8. Password invalid");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "pass wrong");
            r4 = assertEquals("-Wrong password, try again", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_User_id_argument_error() {
        System.out.println("9. User-id, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user7 user7");
            r3 = assertEquals("ERROR: Invalid Arguments\nUsage: USER user-id", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(3));
            testOutcome = (r1 && r2 && r3 && r4) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Account_argument_error() {
        System.out.println("10. Account, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "acct acct2 acct2");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: ACCT account", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Password_argument_error() {
        System.out.println("11. Password, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "pass wrong wrong");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: PASS password", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Type() {
        System.out.println("12. Type");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "type a");
            r4 = assertEquals("+Using Ascii mode", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "type b");
            r5 = assertEquals("+Using Binary mode", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "type c");
            r6 = assertEquals("+Using Continuous mode", sftpClient.getServerResHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Type_argument_error() {
        System.out.println("13. Type, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "type a a");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: TYPE { A | B | C }", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "type d");
            r5 = assertEquals("-Type not valid", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_standard_current_directory() {
        System.out.println("14. List standard, current directory");
        SFTPClient sftpClient = new SFTPClient();
        List<String> expectedFiles = Arrays.asList("+user1/", "file2.txt", "file.txt", "file3.txt", "file1.txt", 
            "temp", "file4.txt", ".DS_Store", "data2.jpg", "data.jpg", "folder1", "license.txt");
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list f");
            r4 =  assertContainsAll(expectedFiles, sftpClient.getServerResHistory().get(3), ANY_NEWLINE);
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_standard_other_directory() {
        System.out.println("15. List standard, other directory");
        SFTPClient sftpClient = new SFTPClient();
        List<String> expectedFiles = Arrays.asList("+user1/temp", "file4.txt", "file5.txt", "data.csv");
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list f temp");
            r4 =  assertContainsAll(expectedFiles, sftpClient.getServerResHistory().get(3), ANY_NEWLINE);
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_verbose_current_directory() {
        System.out.println("16. List verbose, current directory");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list v");
            r4 =  true; // tested visually
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_non_existent_directory() {
        System.out.println("17. List, non-existent directory");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list f fake");
            r4 = assertEquals("-Cant list directory because: user1/fake does not exist", 
                sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_file_instead_of_directory() {
        System.out.println("18. List file instead of directory");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list f license.txt");
            r4 = assertEquals("-Cant list directory because: user1/license.txt is not a directory", 
                sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_List_argument_error() {
        System.out.println("19. List, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "list f / /");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: LIST { F | V } directory-path", 
                sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "list g");
            r5 = assertEquals("-Argument error", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_relative_path() {
        System.out.println("20. Change directory, relative path");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("!Changed working dir to user1/folder1", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "cdir folder2");
            r5 = assertEquals("!Changed working dir to user1/folder1/folder2", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_user_path() {
        System.out.println("21. Change directory, user root");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir /");
            r4 = assertEquals("!Changed working dir to user1/", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_absolute_path() {
        System.out.println("22. Change directory, absolute path");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir /folder1/folder2");
            r4 = assertEquals("!Changed working dir to user1/folder1/folder2", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_non_existent_directory() {
        System.out.println("23. Change directory, non-existent directory");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir /folder1/folder2/folder3");
            r4 = assertEquals("-Cant connect to directory because: user1/folder1/folder2/folder3 does not exist", 
                sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_file_instead_of_directory() {
        System.out.println("24. Change directory, file instead of directory");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir temp/data.csv");
            r4 = assertEquals("-Cant list directory because: user1/temp/data.csv is not a directory", 
                sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_account_required() {
        System.out.println("25. Change directory, account required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user2");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("!Account valid, logged-in\n!Changed working dir to user2/folder1", 
                sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_password_required() {
        System.out.println("26. Change directory, password required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user3");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "pass pass3");
            r5 = assertEquals("!Logged in\n!Changed working dir to user3/folder1", 
                sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_account_and_password_required() {
        System.out.println("27. Change directory, account and password required");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("+Account valid, send password", sftpClient.getServerResHistory().get(4));
            evalClientCommand(sftpClient, "pass pass4");
            r6 = assertEquals("!Logged in\n!Changed working dir to user4/folder1", sftpClient.getServerResHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Change_directory_argument_error() {
        System.out.println("28. Change directory, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getServerResHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getServerResHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getServerResHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1 folder2");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: CDIR new-directory",
                    sftpClient.getServerResHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getServerResHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }
}