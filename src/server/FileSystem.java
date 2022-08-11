package server;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.Files;

import utils.Utils;

public final class FileSystem {
    private static final String HOME_DIR = "resources/home/";
    private static final String USER_DB = "resources/users.txt";
    private static final int USER_ID_COL = 0;
    private static final int ACCOUNT_COL = 1;
    private static final int PASSWORD_COL = 2;
    private static List<User> users = readUsers();

    private FileSystem() {
        throw new IllegalAccessError("server.Utils cannot be instantiated");
    }

    public static String getHomeDir() {
        return HOME_DIR;
    }

    public static User getUser(String userId) {
        for (User user : FileSystem.users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public static boolean dirExists(String relativePath) {
        Path dir = Paths.get(HOME_DIR + relativePath);
        return Files.exists(dir);
    }

    public static boolean pathIsFile(String relativePath) {
        Path filePath = Paths.get(HOME_DIR + relativePath);
        return !Files.isDirectory(filePath);
    }

    public static String readDir(String relativePath) {
        Path dir = Paths.get(HOME_DIR + relativePath);
        String[] fileNames = dir.toFile().list();

        StringBuilder dirListBuilder = new StringBuilder(relativePath + "\n");
        for (int i = 0; i < fileNames.length; i++) {
            dirListBuilder.append(fileNames[i]);
            if (i < (fileNames.length - 1)) {
                dirListBuilder.append("\n");
            }
        }
        return dirListBuilder.toString();
    }

    public static String readDirVerbose(String relativePath) {
        Path dir = Paths.get(HOME_DIR + relativePath);
        File[] files = dir.toFile().listFiles();

        StringBuilder dirListBuilder = new StringBuilder(relativePath + "\n");
        relativePath = Utils.appendIfMissing(relativePath, "/");
        for (int i = 0; i < files.length; i++) {
            dirListBuilder.append("Name: " + files[i].getName() + 
                "    Path: " + relativePath + files[i].getName() + 
                "    Size: " + files[i].length() + " Bytes"
            );
            if (i < (files.length - 1)) {
                dirListBuilder.append("\n");
            }
        }
        return dirListBuilder.toString();
    }

    public static void writeFile(String relativeFilePath, String data) {
        try {
            Path filePath = Paths.get(HOME_DIR + relativeFilePath);
            Files.write(filePath, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static boolean deleteFile(String relativeFilePath) {
        Path filePath = Paths.get(HOME_DIR + relativeFilePath);
        return filePath.toFile().delete();
    }

    private static List<User> readUsers() {
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
}
