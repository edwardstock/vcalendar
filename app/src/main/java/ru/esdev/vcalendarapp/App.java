package ru.esdev.vcalendarapp;

import android.app.Application;

import ru.esdev.vcalendar.VCalendar;
import timber.log.Timber;

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
