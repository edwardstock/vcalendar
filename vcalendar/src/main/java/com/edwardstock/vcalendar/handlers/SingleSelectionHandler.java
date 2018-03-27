package com.edwardstock.vcalendar.handlers;

import android.view.View;

import com.edwardstock.vcalendar.models.CalendarDay;

import java.util.List;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class SingleSelectionHandler extends BaseHandler {
    public SingleSelectionHandler(SelectionDispatcher factory) {
        super(factory);
    }

    @Override
    public void setSelections(List<CalendarDay> selections) {
        if (selections.size() == 0 || selections.get(0) == null) {
            return;
        }

        setSelectionsInternal(selections.get(0));
    }

    @Override
    public void onClick(View dayView, CalendarDay calendarDay) {
        setSelectionsInternal(calendarDay);

        getDispatcher().callOnSelectionListeners(false);
    }

    private void setSelectionsInternal(CalendarDay calendarDay) {
        getDispatcher().clearSelections();
        calendarDay.setSelected(true);
        getDispatcher().getSelections().add(calendarDay);

        getDispatcher().updateSelections();
    }
}
