package com.edwardstock.vcalendar;

import com.edwardstock.vcalendar.models.CalendarDay;

import java.util.List;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface OnSelectionListener {
    void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded);
}
