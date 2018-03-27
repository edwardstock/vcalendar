package com.edwardstock.vcalendar.handlers;

import android.view.View;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.models.CalendarDay;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class MultipleSelectionHandler extends BaseHandler {

    public MultipleSelectionHandler(SelectionDispatcher factory) {
        super(factory);
    }

    @Override
    public void setSelections(List<CalendarDay> selections) {
        final Set<CalendarDay> unique = new HashSet<>(selections);
        final List<CalendarDay> data = Stream.of(unique).filter(item -> item != null).map(item -> getDispatcher().getDayOrCreate(item.getDateTime())).map(item -> item.setSelected(true)).toList();

        getDispatcher().getSelections().clear();
        getDispatcher().getSelections().addAll(data);

        unique.clear();
        data.clear();
    }

    @Override
    public void onClick(View dayView, CalendarDay calendarDay) {
        // reset selection after second click and select last selected day
        if (getDispatcher().getSelections().contains(calendarDay)) {
            calendarDay.setSelected(false);
            getDispatcher().getSelections().remove(calendarDay);
            getDispatcher().updateSelections();
            return;
        }

        if (getDispatcher().hasSelectionLimit() && getDispatcher().getSelections().size() == getDispatcher().getLimit()) {
            getDispatcher().callOnSelectionListeners(true);
            return;
        }

        calendarDay.setSelected(true);
        getDispatcher().getSelections().add(calendarDay);

        // if more than 1 day, sorting
        if (getDispatcher().getSelections().size() > 1) {
            Collections.sort(getDispatcher().getSelections());
        }

        getDispatcher().updateSelections();

        getDispatcher().callOnSelectionListeners(false);
    }
}
