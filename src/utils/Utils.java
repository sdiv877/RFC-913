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

    public static void waitForEnterKey() {
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        scan.close();
    }
}
