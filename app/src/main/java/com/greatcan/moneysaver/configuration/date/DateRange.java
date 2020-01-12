package com.greatcan.moneysaver.configuration.date;

import java.util.Date;

public class DateRange {

    /**
     * Check date in selected range
     * @param startDate
     * @param endDate
     * @param targetDate
     * @return
     */
    public static boolean isDateRange(Date startDate, Date endDate, Date targetDate) {
        return targetDate.compareTo(startDate) >= 0 && targetDate.compareTo(endDate) <= 0;
    }
}
