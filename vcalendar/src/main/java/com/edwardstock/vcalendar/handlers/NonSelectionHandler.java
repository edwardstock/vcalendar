package com.edwardstock.vcalendar.handlers;

import android.view.View;

import com.edwardstock.vcalendar.models.CalendarDay;

import java.util.List;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class NonSelectionHandler extends BaseHandler {
    public NonSelectionHandler(SelectionDispatcher factory) {
        super(factory);
    }

    @Override
    public void setSelections(List<CalendarDay> selections) {
    }

    @Override
    public void onClick(View dayView, CalendarDay calendarDay) {
    }
}
