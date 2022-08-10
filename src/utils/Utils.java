package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Utils {
    public static ArrayList<String> splitString(String s, String regex) {
        ArrayList<String> splitUpString = new ArrayList<String>();
        Collections.addAll(splitUpString, s.split(regex));
        return splitUpString;
    }

    public static String appendIfMissing(String s, String toAppend) {
        if (!s.endsWith(toAppend)) {
            return s + toAppend;
        }
        return s;
    }

    public static String removeIfEndsWith(String s, String toRemove) {
        if (s.endsWith(toRemove)) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static void waitForEnterKey() {
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        scan.close();
    }
}
