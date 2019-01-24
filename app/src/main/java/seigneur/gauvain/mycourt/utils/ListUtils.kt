package seigneur.gauvain.mycourt.utils

import java.util.ArrayList
import java.util.Arrays
import java.util.regex.Matcher
import java.util.regex.Pattern

object ListUtils {

    @JvmStatic
    fun mapToListKey(env: Map<String, String>): List<String> {
        val result = ArrayList<String>()
        for ((key) in env)
            result.add(key)
        return result
    }

}
