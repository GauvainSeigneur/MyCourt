package seigneur.gauvain.mycourt.utils;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class RoomConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }


    @TypeConverter
    public static String stringArrayToString(String[] arrayString) {
        Gson gson = new Gson();
        return arrayString == null ? null : gson.toJson(arrayString);
    }

    @TypeConverter
    public static String[] stringToArrayString(String string) {
        Type listType = new TypeToken<String[]>() {}.getType();
       return new Gson().fromJson(string, listType);
    }

    @TypeConverter
    public static ArrayList<String> arrayListFromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String stringFromArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static Map<String, String> stringToMapString(String string) {
        Type listType = new TypeToken<Map<String, String>>() {}.getType();
        return new Gson().fromJson(string, listType);
    }

    @TypeConverter
    public static String mapStringToString(Map<String, String> mapString) {
        Gson gson = new Gson();
        return mapString == null ? null : gson.toJson(mapString);
    }

   /* @TypeConverter
    public static mapToString(Map<String, String>) {
        return value == null ? null : Gson.toJson(value);
    }

    @TypeConverter
    public static Map<NexoIdentifier, Date> fromMap(String value) {
        return value == null ? null : Gson.fromJson(value, ...);
    }*/



}