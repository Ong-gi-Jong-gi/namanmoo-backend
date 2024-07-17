package ongjong.namanmoo.service;

import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtil {

    private DateUtil() {};

    public static DateUtil getInstance() {
        return DateUtilHolder.INSTANCE;
    }

    private static class DateUtilHolder {
        private static final DateUtil INSTANCE = new DateUtil();
    }

    /**
     * 년,월,일
     */
    public static String FORMAT_1 = "yyyyMMdd";
    public static String FORMAT_2 = "yyyy-MM-dd";
    public static String FORMAT_3 = "yyyy/MM/dd";
    public static String FORMAT_4 = "yyyy.MM.dd";


    /**
     * 년,월,일,시간,분,초
     */
    public static String FORMAT_6 = "yyyyMMddHHmmss";
    public static String FORMAT_7 = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_8 = "yyyy/MM/dd HH:mm:ss";
    public static String FORMAT_9 = "yyyy.MM.dd HH:mm:ss";

    // (1) 날짜시간 데이터를 지정한 포맷의 문자열로 리턴

    /**
     * 현재 시간을 지정한 날짜시간 포맷의 문자열로 리턴하기
     */
    public String getNowStr(String format) {
        if (format==null || format.equals("")) return null;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * 지정한 타임스탬프를 지정한 날짜시간 포맷의 문자열로 리턴하기
     */
    public String getDateStr(long timeStamp, String format) {
        if (format==null || format.equals("")) return null;
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }


    // (2) 날짜시간 포맷의 문자열을 날짜시간 데이터 타입으로 리턴

    /**
     * 날짜시간 포맷의 문자열을 Date로 리턴
     * @throws ParseException
     */
    public Date getDate(String dateStr, String format) throws ParseException {
        if ((dateStr==null || dateStr.equals(""))
                || (format==null || format.equals(""))) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(dateStr);
    }


    /**
     * 날짜시간 포맷의 문자열을 타임스탬프로 리턴
     */
    public Long getTimeStamp(String dateStr, String format) {
        if ((dateStr==null || dateStr.equals(""))
                || (format==null || format.equals(""))) return null;
        return Timestamp.valueOf(dateStr).getTime();
    }

    // 타임스탬프를 "yyyy.MM.dd" 형식의 문자열로 변환
    @Transactional(readOnly = true)
    public String timestampToString(Long challengeDate) {
        DateUtil dateUtil = DateUtil.getInstance();
        return dateUtil.getDateStr(challengeDate, DateUtil.FORMAT_4);
    }

    // "yyyy.MM.dd" 형식의 문자열을 타임스탬프로 변환
    @Transactional(readOnly = true)
    public Long stringToTimestamp(String answerDate, String format) {
        try {
            if ((answerDate == null || answerDate.isEmpty()) || (format == null || format.isEmpty())) {
                return null;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate date = LocalDate.parse(answerDate, formatter);
            LocalDateTime dateTime = date.atStartOfDay(); // LocalDate를 LocalDateTime으로 변환 (00:00:00)
            return Timestamp.valueOf(dateTime).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // "yyyy.MM.dd" 형식의 문자열에 날짜 더하기
    @Transactional(readOnly = true)
    public String addDaysToStringDate(String strChallengeDate, int days) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_4);
        LocalDate date = LocalDate.parse(strChallengeDate, formatter); // 문자열을 LocalDate로 변환
        LocalDate newDate = date.plusDays(days); // 날짜에 days를 더하기
        return newDate.format(formatter); //
    }

    // 두 문자열로 들어오는 날짜의 차이 계산
    @Transactional(readOnly = true)
    public Long getDateDifference(String dateStr1, String dateStr2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_4);

        // 문자열을 LocalDate로 변환
        LocalDate date1 = LocalDate.parse(dateStr1, formatter);
        LocalDate date2 = LocalDate.parse(dateStr2, formatter);

        // 두 날짜의 차이 계산
        return ChronoUnit.DAYS.between(date1, date2) + 1;
    }

    // 두 날짜 시간의 차이 계산
    @Transactional(readOnly = true)
    public long getTimeDifference(String dateTimeStr1, String dateTimeStr2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_9);
        LocalDateTime dateTime1 = LocalDateTime.parse(dateTimeStr1, formatter);
        LocalDateTime dateTime2 = LocalDateTime.parse(dateTimeStr2, formatter);
        return ChronoUnit.SECONDS.between(dateTime1, dateTime2);
    }

}