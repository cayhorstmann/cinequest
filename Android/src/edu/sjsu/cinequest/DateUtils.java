package edu.sjsu.cinequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DateUtils {
    public static final int NORMAL_MODE = 0;
    public static final int FESTIVAL_TEST_MODE = 1;
    public static final int OFFSEASON_TEST_MODE = 2;
    // TODO: Set to NORMAL_MODE before release
    private static int mode = FESTIVAL_TEST_MODE;

    /*
     * If you need more time formats, add a constant here, and be sure to
     * update LAST_TIME_FORMAT to equal the value of the last one. 
     * Also add the formatter below.
     */
    
    public static final int TIME_SHORT = 0;
    // More time formats
    private static final int LAST_TIME_FORMAT = 0; 
    
    public static final int DATE_DEFAULT = 1;
    
    /*
     * If you need more date or date/time formats, add a constant here. 
     * Also add the formatter below. Each formatter's index should match
     * the formatter constant.
     */
    
    private DateFormat[] formatters = {
    	DateFormat.getTimeInstance(DateFormat.SHORT),
    	DateFormat.getDateInstance()
    };
    
    private static String[] festivalDates =
    { 
    "2010-02-23", "2010-02-24", "2010-02-25", "2010-02-26", 
    "2010-02-27", "2010-02-28", "2010-03-01", "2010-03-02",
    "2010-03-03", "2010-03-04", "2010-03-05", "2010-03-06",
    "2010-03-07"
    };
    
    private static Map<String, String> cache = new ConcurrentHashMap<String, String>();
    
    /**
     * Get all dates for this festival in the format yyyy-MM-dd
     * @return an array of all dates
     */
    public static String[] getFestivalDates()
    {
        return festivalDates;
    }
    
    /**
     * Set the festival dates
     * @param fdates a String[] containing each festival date (like "2009-02-26") in order.
     */
    public static void setFestivalDates(String[] fdates)
    {
    	festivalDates = fdates;
    }
 
    /**
     * Checks whether we are in off-season
     * @return if today's date indicates that we should display the app in off-season mode
     */
    public static boolean isOffSeason()
    {
        return today().compareTo(festivalDates[festivalDates.length - 1]) > 0;
    }
    
    /**
     * Sets the mode for date/time reporting
     * @param mode one of NORMAL_MODE, FESTIVAL_TEST_MODE, OFFSEASON_TEST_MODE
     */
    public static void setMode(int mode)
    {
        DateUtils.mode = mode;
    }
    
    /**
     * Gets the mode for date/time reporting
     * @return one of NORMAL_MODE, FESTIVAL_TEST_MODE, OFFSEASON_TEST_MODE
     */
    public static int getMode()
    {
        return mode;
    }
    
    /**
     * Formats a date string into a locale-specific version. 
     * (Note: This is not a static method for thread safety)
     * @param date a string in the format yyyy-MM-dd HH:mm or yyyy-MM-dd
     * @target a format constant defined in this class.
     * TODO: Do we really need thread safety, or can we just restrict this
     * class to the UI thread?
     */
    public String format(String date, int format)
    {
    	int len = date.length();
    	if (format <= LAST_TIME_FORMAT) 
    		date = len == 10 ? "00:00" : date.substring(11, 16);
    	try
        {
    		String key = date + (char)('A' + format);
    		String result = cache.get(key);
    		if (result == null)
    		{
    			String d = format <= LAST_TIME_FORMAT ? "2000-01-01 " + date : date; 
    			String fmt = len == 10 ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm";
    			SimpleDateFormat sdf = new SimpleDateFormat(fmt);
    			result = formatters[format].format(sdf.parse(d));
    			cache.put(key, result);
    		}
        	return result;
        } 
        catch (ParseException ex)
        {
        	return date;
        }
    }
    
    /**
     * Returns today's date
     * @return today's date in yyyy-MM-dd format
     */
    public static String today()
    {
        if (mode == FESTIVAL_TEST_MODE) return festivalDates[5];
        if (mode == OFFSEASON_TEST_MODE) return "2099-12-31"; 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
