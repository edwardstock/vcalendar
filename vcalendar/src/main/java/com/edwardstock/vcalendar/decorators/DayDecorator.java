package com.edwardstock.vcalendar.decorators;

import com.edwardstock.vcalendar.adapter.DayViewFacade;
import com.edwardstock.vcalendar.adapter.Neighbourhood;
import com.edwardstock.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface DayDecorator {
    boolean shouldDecorate(CalendarDay calendarDay);
    void decorate(final CalendarDay calendarDay, final DayViewFacade dayView, final @Neighbourhood int neighbourhood);
}
