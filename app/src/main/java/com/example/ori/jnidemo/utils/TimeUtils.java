package com.example.ori.jnidemo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author: hblolj
 * @Date: 2018/5/7 14:52
 * @Description:
 * @Version: 1.0
 **/
public class TimeUtils {

    public static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");

    public static final SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static String date2String(Date date , SimpleDateFormat format){
        return format.format(date);
    }

    public static Date string2Date(String time, SimpleDateFormat format) throws ParseException {
        return format.parse(time);
    }

    /**
     * 获取时间差 秒
     * @param fromDate
     * @param toDate
     * @param format
     * @return
     * @throws ParseException
     */
    public static Long getTimeInterval(String fromDate, String toDate, SimpleDateFormat format) throws ParseException {
        Date fDate = format.parse(fromDate);
        Date tDate = format.parse(toDate);
        return getTimeInterval(fDate, tDate);
    }

    /**
     * 获取时间差 秒
     * @param fromDate
     * @param toDate
     * @return
     */
    public static Long getTimeInterval(Date fromDate, Date toDate){
        long from = fromDate.getTime();
        long to = toDate.getTime();
        int minutes = (int) ((to - from)/(1000 * 60));
        int second = (int) ((to - from)%(1000 * 60))/1000;
        return Integer.valueOf(minutes * 60 + second).longValue();
    }

    /**
     * 获取当前时间到第二天凌晨的秒数
     * @return
     */
    public static Long getSecondsNextEarlyMorning(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        // 改成这样就好了
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds;
    }

    /**
     * 获取当前时间到第二天中午12点的秒数
     * @return
     */
    public static Long getSecondsNextEarlyMidday(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        // 改成这样就好了
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, 12);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds;
    }

    /**
     * 获取当前时间到今天中午12点的秒数
     * @return
     */
    public static Long getSecondsTodayMidday(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, 12);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds;
    }

    /**
     * 判断当前是上午还是下午(已当天中午12点为基准判断)
     * @return
     */
    public static Boolean getNowIsAfterNoon(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, 12);
        Date midday = cal.getTime();
        Date now = new Date();
        if (now.after(midday)){
            return true;
        }
        return false;
    }

    /**
     * 计算到当天某个时间点的时间差
     * @param hour
     * @return
     */
    public static Long calculateNextTimeDiffInToday(Integer hour){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds;
    }

    /**
     * 计算到次日某个时间点的时间差
     * @param hour
     * @return
     */
    public static Long calculateNextTimeDiffInNextDay(Integer hour){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds;
    }

    public static Date getNowAfterTime(Integer duration, Integer unit){
        Calendar calendar = Calendar.getInstance();
        calendar.add(unit, duration);
        return calendar.getTime();
    }

    public static Date getTodayBegin(){
        Calendar cal = new GregorianCalendar();
        getBegin(cal, Calendar.MINUTE, Calendar.SECOND);
        return cal.getTime();
    }

    public static Date getTodayEnd(){
        Calendar cal = new GregorianCalendar();
        getEnd(cal, Calendar.MINUTE, Calendar.SECOND);
        return cal.getTime();
    }

    public static Date getBeginDayOfWeek() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek);
        getBegin(cal, Calendar.MINUTE, Calendar.SECOND);
        return cal.getTime();
    }

    public static Date getEndDayOfWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        getEnd(cal, Calendar.MINUTE, Calendar.SECOND);
        Date weekEndSta = cal.getTime();
        return weekEndSta;
    }

    private static void getBegin(Calendar cal, int minute, int second) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(minute, 0);
        cal.set(second, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static void getEnd(Calendar cal, int minute, int second) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(minute, 59);
        cal.set(second, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }
}
