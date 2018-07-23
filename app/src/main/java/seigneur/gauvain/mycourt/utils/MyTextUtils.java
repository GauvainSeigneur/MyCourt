package seigneur.gauvain.mycourt.utils;

public class MyTextUtils {

    public MyTextUtils(){}

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

}
