package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public final class Utils {
    private Utils() {
        throw new IllegalAccessError("utils.Utils cannot be instantiated");
    }

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

    /**
     * Performs bound checks to ensure that beginIndex and endIndex are kept within
     * the length of str, and then calls substring normally.
     * 
     * @return the resulting substring
     */
    public static String safeSubstring(String str, int beginIndex, int endIndex) {
        if (endIndex > str.length()) {
            endIndex = str.length();
        }
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        return str.substring(beginIndex, endIndex);
    }

    /**
     * Unescapes the newline and null terminating characters in a string to make
     * them visible.
     * 
     * @return the unescaped string
     */
    public static String unEscapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
            switch (s.charAt(i)) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\0':
                    sb.append("\\0");
                    break;
                default:
                    sb.append(s.charAt(i));
            }
        return sb.toString();
    }

    /**
     * Stops execution of the program until an enter keypress is received from the
     * keyboard stream.
     */
    public static void waitForEnterKey() {
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        scan.close();
    }

    public static void logMessage(String msg) {
        System.out.println(msg);
    }
}
