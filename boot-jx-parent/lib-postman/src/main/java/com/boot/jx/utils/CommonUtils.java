package com.boot.jx.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;

public final class CommonUtils {

	
public static String monthNameByTimestamp(long timestamp) {
		
        String monthYear = null;
        System.out.println("monthYear :"+monthYear);
        
        // Convert the Unix timestamp to LocalDateTime
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

        // Get the month name
        String month = dateTime1.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Get the year
        int year = dateTime1.getYear();
        monthYear =month+" "+year;
        System.out.println("monthYear v1:"+monthYear);
        
        return monthYear;
	}
	
	
	public static long startTStampForaMonth(long givenTimestamp) {
		
	 // Convert the given timestamp to LocalDateTime
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(givenTimestamp), ZoneId.systemDefault());
    
    // Get the start of the month (1st day of that month at 00:00:00)
    LocalDateTime startOfMonth = dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    
    // Get the end of the month (Last day of that month at 23:59:59)
    LocalDateTime endOfMonth = dateTime.withDayOfMonth(dateTime.toLocalDate().lengthOfMonth())
                                       .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    
    // Convert to timestamps (milliseconds since epoch)
    long startTimestamp = startOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
    long endTimestamp = endOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
    
    
    return startTimestamp; 
	}
	
	
	public static long endTStampForaMonth(long givenTimestamp) {
		
		 // Convert the given timestamp to LocalDateTime
	    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(givenTimestamp), ZoneId.systemDefault());
	    
	    // Get the start of the month (1st day of that month at 00:00:00)
	    LocalDateTime startOfMonth = dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
	    
	    // Get the end of the month (Last day of that month at 23:59:59)
	    LocalDateTime endOfMonth = dateTime.withDayOfMonth(dateTime.toLocalDate().lengthOfMonth())
	                                       .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
	    
	    // Convert to timestamps (milliseconds since epoch)
	    long startTimestamp = startOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
	    long endTimestamp = endOfMonth.toInstant(ZoneOffset.UTC).toEpochMilli();
	   
	    return endTimestamp; 
		}


}
