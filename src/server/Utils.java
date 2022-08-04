package server;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public final class Utils {
    private static final String USER_DB = "resources/users.txt";
    private static final int USER_ID_COL = 0;
    private static final int ACCOUNT_COL = 1;
    private static final int PASSWORD_COL = 2;

    private Utils() {
        throw new IllegalAccessError("server.Utils cannot be instantiated");
    }

    public static void logMessage(String msg) {
        System.out.println(msg);
    }

    public static String makeResponse(String msg, ResponseCode responseCode) {
        return responseCode.toString() + msg + "\n";
    }

    public static List<User> readUserDb() {
        List<String> userData = new ArrayList<String>();
        List<User> users = new ArrayList<User>();

        try {
            Path path = Paths.get(USER_DB);
            userData = Files.readAllLines(path);
        } catch (Exception e) {
            System.out.println("Could not open " + USER_DB);
            e.printStackTrace();
            return users;
        }

        // first two lines are column names/divider so remove them
        userData.remove(0);
        userData.remove(0);
        // parse out values in each row
        for (String row : userData) {
            String[] cols = row.split("\\|");
            // get individual values from each column in row
            String userId = cols[USER_ID_COL];
            List<String> accounts = splitString(cols[ACCOUNT_COL], "\\s+");
            List<String> passwords = splitString(cols[PASSWORD_COL], "\\s+");
            // create new user based on values
            User user = new User(userId, accounts, passwords);
            users.add(user);
        }
        return users;
    }

    public static ArrayList<String> splitString(String s, String regex) {
        ArrayList<String> splitUpString = new ArrayList<String>();
        Collections.addAll(splitUpString, s.split(regex));
        return splitUpString;
    }
}
