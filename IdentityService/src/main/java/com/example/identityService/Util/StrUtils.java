package com.example.identityService.Util;

import java.text.Normalizer;

public class StrUtils {
        public static String encodeKeyword(String keyword) {
            if (keyword == null) {
                return "";
            }
            String normalizedKeyword = Normalizer.normalize(keyword, Normalizer.Form.NFD);
            normalizedKeyword = normalizedKeyword.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // Remove accents
            // Escape special characters %, _
            normalizedKeyword = normalizedKeyword.replaceAll("([%_])", "\\\\$1");

            // Add '%' for LIKE query and convert to lower case
            return "%" + normalizedKeyword.toLowerCase() + "%";
        }
}
