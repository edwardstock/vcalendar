package ru.esdev.vcalendar;

import java.util.List;

import ru.esdev.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface OnSelectionListener {
    void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded);
}
