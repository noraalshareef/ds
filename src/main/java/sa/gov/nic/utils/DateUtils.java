// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.utils;

import org.slf4j.LoggerFactory;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import org.slf4j.Logger;

public final class DateUtils
{
    private static final Logger logger;
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String GREENWICH_MEAN_TIME = "Etc/GMT";
    
    private DateUtils() {
    }
    
    public static boolean isAlmostNow(final Date date) {
        final boolean inRange = isInRangeOneMinute(new Date(), date);
        DateUtils.logger.debug("Is almost now: " + inRange);
        return inRange;
    }
    
    private static boolean isInRangeOneMinute(final Date date1, final Date date2) {
        final int oneMinuteInSeconds = 60;
        return isInRangeSeconds(date1, date2, 60);
    }
    
    public static boolean isInRangeMinutes(final Date date1, final Date date2, final int rangeInMinutes) {
        final int rangeInSeconds = rangeInMinutes * 60;
        return isInRangeSeconds(date1, date2, rangeInSeconds);
    }
    
    public static long differenceInMinutes(final Date date1, final Date date2) {
        return TimeUnit.MILLISECONDS.toMinutes(Math.abs(date1.getTime() - date2.getTime()));
    }
    
    private static boolean isInRangeSeconds(final Date date1, final Date date2, final int rangeInSeconds) {
        final Date latestTime = org.apache.commons.lang3.time.DateUtils.addSeconds(date2, rangeInSeconds);
        final Date earliestTime = org.apache.commons.lang3.time.DateUtils.addSeconds(date2, -rangeInSeconds);
        return date1.before(latestTime) && date1.after(earliestTime);
    }
    
    public static SimpleDateFormat getDateFormatterWithGMTZone() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
        return sdf;
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DateUtils.class);
    }
}
