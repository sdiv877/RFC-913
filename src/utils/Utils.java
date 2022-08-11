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

    public static String unEscapeString(String s){
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<s.length(); i++)
			switch (s.charAt(i)){
				case '\n': sb.append("\\n"); break;
				case '\0': sb.append("\\0"); break;
				default: sb.append(s.charAt(i));
			}
		return sb.toString();
	}

    public static void waitForEnterKey() {
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        scan.close();
    }
}
