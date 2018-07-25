package seigneur.gauvain.mycourt.utils;

public class MyTextUtils {

    public MyTextUtils(){}

    /**
     * remove <br> from html text
     * @param text
     * @return
     */
    public static CharSequence noTrailingwhiteLines(CharSequence text) {
        if(text.length()>0){
            while (text.charAt(text.length() - 1) == '\n') {
                text = text.subSequence(0, text.length() - 1);
            }
            return text;
        } else {
            return "";
        }

    }

    //Check if words are between two double quotes
    //if true, don't split them if they are separated by space or comma
    //if false, split them if they are separated by space or comma
    public static String tagRegex = "(\"([^\"]*)\"|[^, ]+)";

    //for checking single quote too
    //String tagRegex = "(\"([^\"]*)\"|'([^']*)|[^, ]+)";

    private static char doublequote= '\"';

    /**
     * Check if the string contains even or odd double quote
     * @param text to be verified
     * @return boolean
     */
    public static boolean isDoubleQuoteCountEven(String text) {
        int doubleQuoteCount = text.replaceAll("[^"+ doublequote +"]", "").length();
        if (doubleQuoteCount== 0 || (doubleQuoteCount % 2) == 0) {
            // number is even or 0
            return true;
        }
        else {
            // number is odd
            return false;
        }
    }
}
