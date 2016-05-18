package com.example.remoteinstallutil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentTime {
	
    public static String getCurrentTime() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
		//Date date = new Date(System.currentTimeMillis());
		String timeString = dateFormat.format(new Date());
				
		return timeString;
	}
}
