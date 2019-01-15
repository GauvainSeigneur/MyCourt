package seigneur.gauvain.mycourt.utils

import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import seigneur.gauvain.mycourt.data.model.Attachment

import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Date


object RoomConverter {

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }


    @TypeConverter
    @JvmStatic
    fun stringArrayToString(arrayString: Array<String>?): String? {
        val gson = Gson()
        return if (arrayString == null) null else gson.toJson(arrayString)
    }

    @TypeConverter
    @JvmStatic
    fun stringToArrayString(string: String): Array<String>? {
        val listType = object : TypeToken<Array<String>>() {

        }.type
        return Gson().fromJson<Array<String>>(string, listType)
    }

    @TypeConverter
    @JvmStatic
    fun arrayListFromString(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.type
        val gson = Gson()
        return gson.fromJson<ArrayList<String>>(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun stringFromArrayList(list: ArrayList<String>?): String? {
        val gson = Gson()
        return if (list == null) null else gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToMapString(string: String?): Map<String, String>? {
        val listType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson<Map<String, String>>(string, listType)
    }

    @TypeConverter
    @JvmStatic
    fun mapStringToString(mapString: Map<String, String>?): String? {
        val gson = Gson()
        return if (mapString == null) null else gson.toJson(mapString)
    }

    @TypeConverter
    @JvmStatic
    fun attachmentListToJson(value: List<Attachment>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun attachmentjsonToList(value: String): List<Attachment>? {
        val objects = Gson().fromJson(value, Array<Attachment>::class.java) as Array<Attachment>
        val list = objects.toList()
        return list
    }

    @TypeConverter
    @JvmStatic
    fun intArrayToString(array: IntArray?): String? {
        val gson = Gson()
        return if (array == null) null else gson.toJson(array)
    }

    @TypeConverter
    @JvmStatic
    fun stringToArrayIntArray(string: String): IntArray? {
        val listType = object : TypeToken<IntArray>() {

        }.type
        return Gson().fromJson<IntArray>(string, listType)
    }



}