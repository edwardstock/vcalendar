package com.edwardstock.vcalendar;

import android.widget.TextView;

import com.edwardstock.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface DayDecorator {
    boolean shouldDecorate(CalendarDay calendarDay);
    void decorate(CalendarDay calendarDay, TextView dayView);
}
