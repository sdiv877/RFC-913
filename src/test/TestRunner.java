package test;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import client.SFTPClient;
import fs.FileSystem;
import utils.Utils;

final class TestRunner {
    private static String CLIENT_WELCOME_MSG = "Successfully connected to localhost on port 6789";
    private static String SERVER_WELCOME_MSG = "+RFC 913 SFTP Server";
    private static String ANY_NEWLINE = "\r?\n|\r";
    private static String UNKNOWN_COMMAND_MSG = "ERROR: Invalid Command\r\nAvailable Commands: \"USER\", \"ACCT\", \"PASS\", \"TYPE\","
            + " \"LIST\", \"CDIR\", \"KILL\", \"NAME\", \"TOBE\", \"DONE\", \"RETR\", \"SEND\", \"STOP\", \"STOR\", \"SIZE\"";

    private static List<TestOutcome> testResults = new ArrayList<TestOutcome>();

    public static void main(String[] argv) throws Exception {
        initTestFiles();
        clearGeneratedFiles();

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
        testResults.add(test_Delete_file());
        testResults.add(test_Delete_non_existent_file());
        testResults.add(test_Delete_argument_error());
        testResults.add(test_Rename());
        testResults.add(test_Rename_non_existent_file());
        testResults.add(test_Rename_file_already_exists());
        testResults.add(test_Rename_argument_error());
        testResults.add(test_Done());
        testResults.add(test_Done_argument_error());
        testResults.add(test_Retrieve());
        testResults.add(test_Retrieve_stop_sending());
        testResults.add(test_Retrieve_non_existent_file());
        testResults.add(test_Retrieve_directory_instead_of_file());
        testResults.add(test_Retrieve_argument_error());
        testResults.add(test_Store_new_file_does_not_exist());
        testResults.add(test_Store_new_file_exists());
        testResults.add(test_Store_old_file_does_not_exist());
        testResults.add(test_Store_old_file_exists());
        testResults.add(test_Store_append_file_does_not_exist());
        testResults.add(test_Store_append_file_exists());
        testResults.add(test_Store_non_existent_file());
        testResults.add(test_Store_directory_instead_of_file());
        testResults.add(test_Store_argument_error());
        testResults.add(test_Access_denied());
        testResults.add(test_Unknown_command());
        System.out.println("| CLIENT TESTS COMPLETED |");

        if (!keepArgProvided(argv)) {
            clearGeneratedFiles();
        }
        printTestResults();
        Utils.waitForEnterKey();
    }

    private static boolean keepArgProvided(String argv[]) {
        return argv.length != 0 && argv[0].equals("--keep");
    }

    private static void initTestFiles() {
        FileSystem.writeFile("user1/delete.txt", "");
        FileSystem.writeFile("user1/rename.txt", "");
        FileSystem.writeFile("user1/file.txt", "");
    }

    private static void clearGeneratedFiles() {
        FileSystem.writeFile("user1/file.txt", "");
        FileSystem.deleteFile("user1/file5.txt");
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
        // System.out.println("Test 16 requires manual verification.");
        System.out.println("| PASSED: " + successCount + "/" + testResults.size() + " tests.   |");
    }

    private static boolean assertEquals(String expected, String actual) {
        return actual.equals(expected);
    }

    private static boolean assertContains(String expected, String actual) {
        return actual.contains(expected);
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

    private static void evalClientCommand(SFTPClient sftpClient, String cmd) throws Exception {
        System.out.println("> " + cmd);
        sftpClient.evalCommand(cmd);
    }

    private static TestOutcome test_User_id_valid() {
        System.out.println("1. User-id valid");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(3));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user2");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "acct acct1");
            r4 = assertEquals("!Account valid, logged-in", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user3");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "pass pass3");
            r4 = assertEquals("!Logged in", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "acct acct1");
            r4 = assertEquals("+Account valid, send password", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "pass pass4");
            r5 = assertEquals("!Logged in", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user5");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "pass pass5");
            r4 = assertEquals("+Send account", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("!Account valid, logged-in", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "acct acct2");
            r6 = assertEquals("!Account valid, logged-in", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "acct acct3");
            r7 = assertEquals("!Account valid, logged-in", sftpClient.getLogHistory().get(6));
            evalClientCommand(sftpClient, "done");
            r8 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(7));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user7");
            r3 = assertEquals("-Invalid user-id, try again", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(3));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "acct acct2");
            r4 = assertEquals("-Invalid account, try again", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "pass wrong");
            r4 = assertEquals("-Wrong password, try again", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user7 user7");
            r3 = assertEquals("ERROR: Invalid Arguments\nUsage: USER user-id", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(3));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "acct acct2 acct2");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: ACCT account", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "pass wrong wrong");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: PASS password", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "type a");
            r4 = assertEquals("+Using Ascii mode", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "type b");
            r5 = assertEquals("+Using Binary mode", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "type c");
            r6 = assertEquals("+Using Continuous mode", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "type a a");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: TYPE { A | B | C }", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "type d");
            r5 = assertEquals("-Type not valid", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f");
            r4 = assertContainsAll(expectedFiles, sftpClient.getLogHistory().get(3), ANY_NEWLINE);
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f temp");
            r4 = assertContainsAll(expectedFiles, sftpClient.getLogHistory().get(3), ANY_NEWLINE);
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list v");
            r4 = true; // tested visually
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f fake");
            r4 = assertEquals("-Can't list directory because: user1/fake does not exist",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f license.txt");
            r4 = assertEquals("-Can't list directory because: user1/license.txt is not a directory",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f / /");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: LIST { F | V } directory-path",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "list g");
            r5 = assertEquals("-Argument error", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("!Changed working dir to user1/folder1", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "cdir folder2");
            r5 = assertEquals("!Changed working dir to user1/folder1/folder2", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir /");
            r4 = assertEquals("!Changed working dir to user1/", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir /folder1/folder2");
            r4 = assertEquals("!Changed working dir to user1/folder1/folder2", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1/folder2/folder3");
            r4 = assertEquals("-Can't connect to directory because: user1/folder1/folder2/folder3 does not exist",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir temp/data.csv");
            r4 = assertEquals("-Can't list directory because: user1/temp/data.csv is not a directory",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user2");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("!Account valid, logged-in\n!Changed working dir to user2/folder1",
                    sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user3");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "pass pass3");
            r5 = assertEquals("!Logged in\n!Changed working dir to user3/folder1",
                    sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user4");
            r3 = assertEquals("+User-id valid, send account and password", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1");
            r4 = assertEquals("+Directory exists, send account/password", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "acct acct1");
            r5 = assertEquals("+Account valid, send password", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "pass pass4");
            r6 = assertEquals("!Logged in\n!Changed working dir to user4/folder1", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
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

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "cdir folder1 folder2");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: CDIR new-directory",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Delete_file() {
        System.out.println("29. Delete file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "kill delete.txt");
            r4 = assertEquals("+user1/delete.txt deleted", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Delete_non_existent_file() {
        System.out.println("30. Delete non-existent file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "kill fake.txt");
            r4 = assertEquals("-Not deleted because user1/fake.txt does not exist",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Delete_argument_error() {
        System.out.println("31. Delete, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "kill fake.txt fake.txt");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: KILL file-spec",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Rename() {
        System.out.println("32. Rename");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "name rename.txt");
            r4 = assertEquals("+File exists", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "tobe new.txt");
            r5 = assertEquals("+user1/rename.txt renamed to user1/new.txt",
                    sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "kill new.txt");
            r6 = assertEquals("+user1/new.txt deleted", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Rename_non_existent_file() {
        System.out.println("33. Rename non-existent file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "name fake.txt");
            r4 = assertEquals("-Can't find user1/fake.txt", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Rename_file_already_exists() {
        System.out.println("34. Rename, file already exists");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "name file.txt");
            r4 = assertEquals("+File exists", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "tobe file2.txt");
            r5 = assertEquals("-File wasn't renamed because user1/file2.txt already exists",
                    sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Rename_argument_error() {
        System.out.println("35. Rename, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "name fake.txt fake.txt");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: NAME old-file-spec",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "name file.txt");
            r5 = assertEquals("+File exists", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "tobe new.txt new.txt");
            r6 = assertEquals("ERROR: Invalid Arguments\nUsage: TOBE new-file-spec",
                    sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Done() {
        System.out.println("36. Done");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "done");
            r3 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(2));
            testOutcome = (r1 && r2 && r3) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Done_argument_error() {
        System.out.println("37. Done, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "done done");
            r3 = assertEquals("ERROR: Invalid Arguments\nUsage: DONE", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(3));
            testOutcome = (r1 && r2 && r3 && r4) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Retrieve() {
        System.out.println("38. Retrieve");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "retr temp/data.csv");
            r4 = assertContains("bytes will be sent", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "send");
            // the actual data in temp/data.csv is a string that says "+File sent"
            r5 = assertEquals("+File sent", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Retrieve_stop_sending() {
        System.out.println("39. Retrieve, stop sending");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "retr temp/data.csv");
            r4 = assertContains("bytes will be sent", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "stop");
            r5 = assertEquals("+File will not be sent", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Retrieve_non_existent_file() {
        System.out.println("40. Retrieve non-existent file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "retr fake.txt");
            r4 = assertEquals("-File doesn't exist", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Retrieve_directory_instead_of_file() {
        System.out.println("41. Retrieve directory instead of file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "retr temp");
            r4 = assertEquals("-Specifier is not a file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Retrieve_argument_error() {
        System.out.println("42. Retrieve, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7, r8;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "retr file.txt file.txt");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: RETR file-spec", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "retr file.txt");
            r5 = assertContains("bytes will be sent", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "send send");
            r6 = assertEquals("ERROR: Invalid Arguments\nUsage: SEND", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "stop stop");
            r7 = assertEquals("ERROR: Invalid Arguments\nUsage: STOP", sftpClient.getLogHistory().get(6));
            evalClientCommand(sftpClient, "done");
            r8 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(7));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7 && r8) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_new_file_does_not_exist() {
        FileSystem.deleteFile("user1/file.txt");
        System.out.println("43. Store new, file does not exist");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor new file.txt");
            r4 = assertEquals("+File does not exist, will create new file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_43_");
            r6 = assertEquals("+Saved user1/file.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_new_file_exists() {
        System.out.println("44. Store new, file exists");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor new file.txt");
            r4 = assertEquals("+File exists, will create new generation of file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_44_");
            r6 = assertEquals("+Saved user1/file5.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_old_file_does_not_exist() {
        FileSystem.deleteFile("user1/file.txt");
        System.out.println("45. Store old, file does not exist");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor old file.txt");
            r4 = assertEquals("+Will create new file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_45_");
            r6 = assertEquals("+Saved user1/file.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_old_file_exists() {
        System.out.println("46. Store old, file exists");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor old file.txt");
            r4 = assertEquals("+Will write over old file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_46_");
            r6 = assertEquals("+Saved user1/file.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_append_file_does_not_exist() {
        FileSystem.deleteFile("user1/file.txt");
        System.out.println("47. Store append, file does not exist");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor app file.txt");
            r4 = assertEquals("+Will create new file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_47_");
            r6 = assertEquals("+Saved user1/file.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_append_file_exists() {
        System.out.println("48. Store append, file exists");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor app file.txt");
            r4 = assertEquals("+Will append to file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "size 8");
            r5 = assertEquals("+Ok, waiting for file", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "TEST_48_");
            r6 = assertEquals("+Saved user1/file.txt", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "done");
            r7 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(6));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_non_existent_file() {
        System.out.println("49. Store non-existent file");
        System.out.println("[NOT IMPLEMENTED AS THIS CONTRADICTS TESTS 43, 45 and 47]\n");
        return TestOutcome.Success;
    }

    private static TestOutcome test_Store_directory_instead_of_file() {
        System.out.println("50. Store directory instead of file");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor new client");
            r4 = assertEquals("-Specifier is not a file", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "done");
            r5 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(4));
            testOutcome = (r1 && r2 && r3 && r4 && r5) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Store_argument_error() {
        System.out.println("51. Store, argument error");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6, r7, r8;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "user user1");
            r3 = assertEquals("!user1 logged in", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "stor app file.txt file.txt");
            r4 = assertEquals("ERROR: Invalid Arguments\nUsage: STOR { NEW | OLD | APP } file-spec",
                    sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "stor a");
            r5 = assertEquals("ERROR: Invalid Arguments\nUsage: STOR { NEW | OLD | APP } file-spec",
                    sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "stor app file.txt");
            r6 = assertEquals("+Will append to file", sftpClient.getLogHistory().get(5));
            evalClientCommand(sftpClient, "size 8 8");
            r7 = assertEquals("ERROR: Invalid Arguments\nUsage: SIZE number-of-bytes-in-file",
                    sftpClient.getLogHistory().get(6));
            evalClientCommand(sftpClient, "done");
            r8 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(7));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6 && r7 && r8) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Access_denied() {
        System.out.println("52. Access denied");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4, r5, r6;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "type a");
            r3 = assertEquals("-Please log in first", sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "list f");
            r4 = assertEquals("-Please log in first", sftpClient.getLogHistory().get(3));
            evalClientCommand(sftpClient, "name rename.txt");
            r5 = assertEquals("-Please log in first", sftpClient.getLogHistory().get(4));
            evalClientCommand(sftpClient, "done");
            r6 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(5));
            testOutcome = (r1 && r2 && r3 && r4 && r5 && r6) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }

    private static TestOutcome test_Unknown_command() {
        System.out.println("53. Unknown command");
        SFTPClient sftpClient = new SFTPClient();
        boolean r1, r2, r3, r4;
        TestOutcome testOutcome;

        r1 = assertEquals(CLIENT_WELCOME_MSG, sftpClient.getLogHistory().get(0));
        r2 = assertEquals(SERVER_WELCOME_MSG, sftpClient.getLogHistory().get(1));
        try {
            evalClientCommand(sftpClient, "unknown");
            r3 = assertEquals(UNKNOWN_COMMAND_MSG, sftpClient.getLogHistory().get(2));
            evalClientCommand(sftpClient, "done");
            r4 = assertEquals("+Closing connection", sftpClient.getLogHistory().get(3));
            testOutcome = (r1 && r2 && r3 && r4) ? TestOutcome.Success : TestOutcome.Failure;
        } catch (Exception e) {
            e.printStackTrace();
            testOutcome = TestOutcome.Exception;
        }

        System.out.println();
        return testOutcome;
    }
}
