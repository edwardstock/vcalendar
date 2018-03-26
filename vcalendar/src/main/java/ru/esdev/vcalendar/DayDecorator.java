package ru.esdev.vcalendar;

import android.view.View;
import android.widget.TextView;

import ru.esdev.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface DayDecorator {
    boolean shouldDecorate(CalendarDay calendarDay);
    void decorate(CalendarDay calendarDay, TextView dayView);
}
