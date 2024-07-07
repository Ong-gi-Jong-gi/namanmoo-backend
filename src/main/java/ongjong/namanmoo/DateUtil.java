package ongjong.namanmoo;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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



}