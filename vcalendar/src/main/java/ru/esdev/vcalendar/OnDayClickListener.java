package ru.esdev.vcalendar;

import android.view.View;

import ru.esdev.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface OnDayClickListener {
    void onClick(View dayView, CalendarDay calendarDay);
}
