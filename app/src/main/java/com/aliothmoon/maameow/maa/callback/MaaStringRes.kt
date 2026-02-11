package com.aliothmoon.maameow.maa.callback

import android.content.res.Resources


object MaaStringRes {

    private val cache = HashMap<String, Int>(64)


    fun getResId(resources: Resources, packageName: String, key: String): Int {
        cache[key]?.let { return it }
        val resName = "maa_${camelToSnake(key)}"
        val resId = resources.getIdentifier(resName, "string", packageName)
        if (resId != 0) {
            cache[key] = resId
        }
        return resId
    }


    fun getString(resources: Resources, packageName: String, key: String): String {
        val resId = getResId(resources, packageName, key)
        return if (resId != 0) resources.getString(resId) else key
    }


    fun getString(
        resources: Resources,
        packageName: String,
        key: String,
        vararg formatArgs: Any,
    ): String {
        val resId = getResId(resources, packageName, key)
        return if (resId != 0) resources.getString(resId, *formatArgs) else key
    }


    internal fun camelToSnake(name: String): String {
        val s = name.replace('.', '_')
        val sb = StringBuilder(s.length + 4)
        for (i in s.indices) {
            val c = s[i]
            if (c.isUpperCase()) {
                if (i > 0 && (s[i - 1].isLowerCase() || s[i - 1].isDigit())) {
                    sb.append('_')
                } else if (i > 0 && i + 1 < s.length && s[i + 1].isLowerCase()
                    && s[i - 1].isUpperCase()
                ) {
                    sb.append('_')
                }
                sb.append(c.lowercaseChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
