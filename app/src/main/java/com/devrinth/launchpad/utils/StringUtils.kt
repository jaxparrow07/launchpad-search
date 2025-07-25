package com.devrinth.launchpad.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class StringUtils {
    companion object {

        fun anyFuzzyContains(query: String, targets: ArrayList<String>): Boolean {
            if (query.isEmpty()) return true
            if (targets.isEmpty()) return false

            for (target in targets) {
                if (fuzzyContains(query, target)) {
                    return true
                }
            }

            return false
        }
        fun fuzzyContains(query: String, target: String): Boolean {
            if (query.isEmpty()) return true
            if (target.isEmpty()) return false

            val queryLower = query.lowercase().trim()
            val targetLower = target.lowercase().trim()

            var queryIndex = 0

            for (char in targetLower) {
                if (queryIndex < queryLower.length && char == queryLower[queryIndex]) {
                    queryIndex++
                }
            }

            return queryIndex == queryLower.length
        }

        fun anyContains(query: String, targets: ArrayList<String>): Boolean {
            for (target in targets) {
                if (simpleContains(query, target)) {
                    return true
                }
            }
            return false
        }
        fun simpleContains(query: String, target: String): Boolean {
            if (query.isEmpty()) return true
            if (target.isEmpty()) return false

            val queryLower = query.lowercase().trim()
            val targetLower = target.lowercase().trim()

            return targetLower.contains(queryLower)
        }



        fun encodeUrl(input: String): String {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
        }

        fun isValidUrl(url: String): Boolean {
            return url.startsWith("http://") || url.startsWith("https://")
        }

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

    }

}