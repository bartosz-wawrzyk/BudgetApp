package com.example.budgetapp.utils;

import java.util.HashMap;
import java.util.Map;

public class MonthUtils {

    private static final Map<String, Integer> MONTHS_MAP = new HashMap<>();

    static {
        MONTHS_MAP.put("Styczeń", 1);
        MONTHS_MAP.put("Luty", 2);
        MONTHS_MAP.put("Marzec", 3);
        MONTHS_MAP.put("Kwiecień", 4);
        MONTHS_MAP.put("Maj", 5);
        MONTHS_MAP.put("Czerwiec", 6);
        MONTHS_MAP.put("Lipiec", 7);
        MONTHS_MAP.put("Sierpień", 8);
        MONTHS_MAP.put("Wrzesień", 9);
        MONTHS_MAP.put("Październik", 10);
        MONTHS_MAP.put("Listopad", 11);
        MONTHS_MAP.put("Grudzień", 12);
    }

    public static Integer getMonthNumber(String monthName) {
        return MONTHS_MAP.get(monthName);
    }

    public static String getMonthName(int monthNumber) {
        for (Map.Entry<String, Integer> entry : MONTHS_MAP.entrySet()) {
            if (entry.getValue() == monthNumber) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map<String, Integer> getMonthsMap() {
        return MONTHS_MAP;
    }
}

