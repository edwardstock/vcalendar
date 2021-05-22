package com.edwardstock.vcalendar.models;

import android.annotation.SuppressLint;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import androidx.annotation.NonNull;

/**
 * vcalendar. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class CalendarDay implements Comparable<CalendarDay> {
    private final DateTime mDateTime;
    private boolean mSelected;
    private int mState;

    public CalendarDay(DateTime dt) {
        mDateTime = dt;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
		return String.format("CalendarDay{num=%02d, month=%02d, year=%04d, selected=%b}", getDay(), mDateTime.getMonthOfYear(), mDateTime.getYear(), isSelected());
	}

	public DateTime getDateTime() {
		return mDateTime;
	}
	@Override
	public int hashCode() {
		return mDateTime.hashCode();
	}
	public int getDay() {
		return mDateTime.getDayOfMonth();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CalendarDay that = (CalendarDay) o;
		return mDateTime.equals(that.getDateTime());
	}
	/**
	 * @return 1-based month, january will be 1
	 */
	public int getMonth() {
		return mDateTime.getMonthOfYear();
	}

	public int getYear() {
		return mDateTime.getYear();
	}

	@Override
	public int compareTo(@NonNull CalendarDay calendarDay) {
		return mDateTime.compareTo(calendarDay.mDateTime);
	}

    public CalendarDay setSelected(boolean selected) {
        mSelected = selected;
        return this;
    }

    public Duration getDiffDuration(CalendarDay day) {
        return new Duration(mDateTime, day.mDateTime);
    }

    public boolean isSelected() {
        return mSelected;
    }

    public int getState() {
        return mState;
    }

    public void toggleSelection() {
        mSelected = !mSelected;
    }
}
