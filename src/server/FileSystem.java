package server;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import utils.Utils;

public final class FileSystem {
    private static final String HOME_DIR = "resources/home/";
    private static final String USER_DB = "resources/users.txt";
    private static final int USER_ID_COL = 0;
    private static final int ACCOUNT_COL = 1;
    private static final int PASSWORD_COL = 2;

    private FileSystem() {
        throw new IllegalAccessError("server.Utils cannot be instantiated");
    }

    public static String getHomeDir() {
        return HOME_DIR;
    }

    public static List<User> readUsers() {
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
            List<String> accounts = Utils.splitString(cols[ACCOUNT_COL], "\\s+");
            String password = cols[PASSWORD_COL].equals(" ") ? null : cols[PASSWORD_COL];
            // create new user based on values
            User user = new User(userId, accounts, password);
            users.add(user);
        }
        return users;
    }

    public static String readDir(String relativePath) {
        Path dir = Paths.get(HOME_DIR + relativePath);
        String[] files = dir.toFile().list();
        
        StringBuilder dirListBuilder = new StringBuilder(relativePath + "\n");
        for (int i = 0; i < files.length; i++) {
            dirListBuilder.append(files[i]);
            if (i < (files.length - 1)) {
                dirListBuilder.append("\n");
            }
        }
        return dirListBuilder.toString();
    }
}
