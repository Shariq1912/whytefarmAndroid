package com.whytefarms.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String getStringFromMillis(long milliSeconds, String dateFormat, boolean shouldLocalize) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, shouldLocalize ? Locale.getDefault() : Locale.ENGLISH);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static Date getDateFromString(String date, String dateFormat, boolean shouldLocalize) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, shouldLocalize ? Locale.getDefault() : Locale.ENGLISH);
        return formatter.parse(date);
    }

    public static long getTimeAfterAddingDays(long time, long days) {
        return time + (24 * 60 * 60 * 1000 * days);
    }

    public static String getWeekDayFromDate(String stringDate, String dateFormat, boolean shouldLocalize) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, shouldLocalize ? Locale.getDefault() : Locale.ENGLISH);
        Date date = formatter.parse(stringDate);

        if (date != null) {
            return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        } else {
            return "";
        }
    }

    public static Date getDateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate getLocalDateFromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime getLocalDateTimeFromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static DateDifference getDifference(Date startDate, Date endDate) {

        DateDifference dateDifference = new DateDifference();
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;
        dateDifference.setElapsedDays(elapsedDays);

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;
        dateDifference.setElapsedHours(elapsedHours);

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;
        dateDifference.setElapsedMinutes(elapsedMinutes);

        long elapsedSeconds = different / secondsInMilli;
        dateDifference.setElapsedSeconds(elapsedSeconds);


        return dateDifference;
    }

    public static String printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;


        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        if (elapsedDays > 0) {
            if (elapsedDays == 1) return elapsedDays + " day";
            else return elapsedDays + " days";
        }

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        if (elapsedHours > 0) {
            if (elapsedHours == 1) return elapsedHours + " hr";
            else return elapsedHours + " hrs";
        }

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        if (elapsedMinutes > 0) {
            if (elapsedMinutes == 1) return elapsedMinutes + " min";
            else return elapsedMinutes + " mins";
        }

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedSeconds == 1) return elapsedSeconds + " sec";
        else return elapsedSeconds + " secs";
    }


    public static class DateDifference {
        private long elapsedDays;
        private long elapsedHours;
        private long elapsedMinutes;
        private long elapsedSeconds;

        public long getElapsedDays() {
            return elapsedDays;
        }

        public void setElapsedDays(long elapsedDays) {
            this.elapsedDays = elapsedDays;
        }

        public long getElapsedHours() {
            return elapsedHours;
        }

        public void setElapsedHours(long elapsedHours) {
            this.elapsedHours = elapsedHours;
        }

        public long getElapsedMinutes() {
            return elapsedMinutes;
        }

        public void setElapsedMinutes(long elapsedMinutes) {
            this.elapsedMinutes = elapsedMinutes;
        }

        public long getElapsedSeconds() {
            return elapsedSeconds;
        }

        public void setElapsedSeconds(long elapsedSeconds) {
            this.elapsedSeconds = elapsedSeconds;
        }
    }

}