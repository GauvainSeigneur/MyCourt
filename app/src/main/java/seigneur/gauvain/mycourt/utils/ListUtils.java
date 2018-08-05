package seigneur.gauvain.mycourt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

}
