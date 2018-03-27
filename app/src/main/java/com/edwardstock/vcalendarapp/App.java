package com.edwardstock.vcalendarapp;

import android.app.Application;

import com.edwardstock.vcalendar.VCalendar;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		VCalendar.initialize(this, true);
	}
}
