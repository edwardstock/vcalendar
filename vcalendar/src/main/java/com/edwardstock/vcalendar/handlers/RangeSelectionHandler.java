package com.edwardstock.vcalendar.handlers;

import android.view.View;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.List;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class RangeSelectionHandler extends BaseHandler {

    public RangeSelectionHandler(SelectionDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void setSelections(List<CalendarDay> selections) {
        getDispatcher().getSelections().clear();

        final List<CalendarDay> s = Stream.of(selections).filter(item -> item != null).map(item -> getDispatcher().getDayOrCreate(item.getDateTime())).map(item -> item.setSelected(true)).toList();

        getDispatcher().getSelections().addAll(s);

        setSelectionsInternal(getDispatcher().getSelections());
    }

    @Override
    public void onClick(View dayView, CalendarDay calendarDay) {
        // reset selections after second click and select last selected day
        if (getDispatcher().selectionClickCount == 2) {
            getDispatcher().clearSelections();
            getDispatcher().selectionClickCount = 0;
        }
        getDispatcher().selectionClickCount++;


        calendarDay.setSelected(true);
        getDispatcher().getSelections().add(calendarDay);

        // if more than 1 day, sorting
        if (getDispatcher().getSelections().size() > 1) {
            Collections.sort(getDispatcher().getSelections());
        }

        // if have selected items
        if (getDispatcher().getSelections().size() > 0) {
            setSelectionsInternal(getDispatcher().getSelections());
        }

        // just in case
        if (getDispatcher().getSelections().size() > 1) {
            Collections.sort(getDispatcher().getSelections());
        }

        getDispatcher().callOnSelectionListeners(getDispatcher().getSelections().size() == getDispatcher().getLimit());
        getDispatcher().updateSelections();
    }

    private void setSelectionsInternal(List<CalendarDay> selections) {
        final CalendarDay first = selections.get(0);
        final CalendarDay last = selections.get(selections.size() - 1);
        // if first day != last day, mean selected real two or more days
        if (!first.equals(last)) {
            // calculating diff between days
            final Duration duration = first.getDiffDuration(last);
            long diff = duration.getStandardDays();

            // clear current selections
            getDispatcher().clearSelections();

            // writing all range by new, with intermediate dates
            for (int i = -1; i < diff; i++) {
                if (getDispatcher().hasSelectionLimit() && i + 1 >= getDispatcher().getLimit()) {
                    break;
                }

                // taking first day
                final DateTime cal = first.getDateTime();
                // adding to first day iteration + 1 day, than we have next day
                // adding days to LocalDate cause we can't set to DateTime 50 january (overflow provided)
                final LocalDate localDate = new LocalDate(cal).plusDays(i + 1);

                // converting LocalDate to DateTime with previous day argument, to calculate dates relatively
                final DateTime newCal = localDate.toDateTime(cal);

                CalendarDay nextDay = getDispatcher().getDayOrCreate(newCal);
                nextDay.setSelected(true);
                selections.add(nextDay);
            }
        }
    }
}
