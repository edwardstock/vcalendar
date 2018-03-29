package com.edwardstock.vcalendar.decorators;

import com.edwardstock.vcalendar.R;
import com.edwardstock.vcalendar.models.CalendarDay;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DefaultDayDecorator extends ConnectedDayDecorator {

    @Override
    public int getSelectedBeginBackgroundRes() {
        return R.drawable.bg_calendar_day_selection_begin;
    }

    @Override
    public int getSelectedMiddleBackgroundRes() {
        return R.drawable.bg_calendar_day_selection_middle;
    }

    @Override
    public int getSelectedEndBackgroundRes() {
        return R.drawable.bg_calendar_day_selection_end;
    }

    @Override
    public int getSelectedSingleBackgroundRes() {
        return R.drawable.bg_calendar_day_selection_single;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return calendarDay.isSelected();
    }
}
