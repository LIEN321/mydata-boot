package tech.zhiwei.tool.date;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.format.FastDateFormat;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间工具类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class DateUtil extends cn.hutool.core.date.DateUtil {
    /**
     * 今天日期格式 /分隔：yyyy/MM/dd
     */
    public static final String TODAY_DATETIME_PATTERN = "yyyy/MM/dd";

    /**
     * 今天日期格式 /分隔 {@link FastDateFormat}：yyyy/MM/dd
     */
    public static final FastDateFormat TODAY_DATETIME_FORMAT = FastDateFormat.getInstance(TODAY_DATETIME_PATTERN);

    /**
     * 更新时间对象的时分秒
     *
     * @param time   时间对象
     * @param hour   新的小时，若null 则不更新
     * @param minute 新的分钟，若null 则不更新
     * @param second 新的秒，若null 则不更新
     * @return 更新后的时间对象
     */
    public static Date updateTime(Date time, Integer hour, Integer minute, Integer second) {
        Calendar calendar = CalendarUtil.calendar(time);
        if (hour != null) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            calendar.set(Calendar.MINUTE, minute);
        }
        if (second != null) {
            calendar.set(Calendar.SECOND, second);
        }
        return calendar.getTime();
    }

    /**
     * 调整时间
     *
     * @param time   原时间
     * @param hour   小时增量
     * @param minute 分钟增量
     * @param second 秒钟增量
     * @return 调整后的时间
     */
    public static Date add(Date time, Integer hour, Integer minute, Integer second) {
        Calendar calendar = CalendarUtil.calendar(time);
        if (hour != null) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);
        }
        if (minute != null) {
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + minute);
        }
        if (second != null) {
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + second);
        }

        return calendar.getTime();
    }

    /**
     * 获取当前时间，精确到毫秒
     *
     * @return 当前时间
     */
    public static String nowInMillis() {
        return format(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN);
    }
}
