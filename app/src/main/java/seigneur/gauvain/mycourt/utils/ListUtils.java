package seigneur.gauvain.mycourt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListUtils {

    public ListUtils(){}

    public static List<String> mapToListKey(Map<String, String> env) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, String> entry : env.entrySet())
            result.add(entry.getKey());
        return result;
    }

    public static List<String> mapToListValue(Map<String, String> env) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, String> entry : env.entrySet())
            result.add(entry.getValue());
        return result;
    }


    public static List<String> mapToList(Map<String, String> env) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, String> entry : env.entrySet())
            result.add(entry.getKey() + " " + entry.getValue());
        return result;
    }

    public static ArrayList<String> tagListWithoutQuote(String tagString) {
        ArrayList<String> listWithQuote = tempTagList(tagString);
        String[] output = new String[listWithQuote.size()];
        StringBuilder builder;
        for (int i = 0; i < listWithQuote.size(); i++) {
            builder = new StringBuilder();
            output[i] = builder.toString();
            output[i] = listWithQuote.get(i).replaceAll("\"", "");
        }

        return new ArrayList<>(Arrays.asList(output));
    }

    //Create taglist according to Dribbble pattern
    public static ArrayList<String> tempTagList(String tagString) {
        ArrayList<String> tempList = new ArrayList<>();
        //create the list just one time, not any time the tags changed
        if (tagString != null && !tagString.isEmpty()) {
            Pattern p = Pattern.compile(MyTextUtils.tagRegex);
            Matcher m = p.matcher(tagString);
            if (MyTextUtils.isDoubleQuoteCountEven(tagString)) {
                // number is even or 0
                while (m.find()) {
                    tempList.add(m.group(0));
                }
            } else {
                //todo-  number is odd: warn user and stop
            }
        }
        return tempList;
    }

}
