package ru.esdev.vcalendar;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.List;

import ru.esdev.vcalendar.adapter.DaysAdapter;
import ru.esdev.vcalendar.models.CalendarDay;
import ru.esdev.vcalendar.adapter.CalendarMonthItem;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface CalendarHandler {

    CalendarDay getDay(DateTime dateTime);
    CalendarDay getDayOrCreate(DateTime dateTime);
    void setMonthRow(CalendarMonthItem calendarMonthItem, DateTime dateTime);
    @NonNull CalendarDay getPreviousDay(CalendarDay current);
    @NonNull CalendarDay getNextDay(CalendarDay current);
    @DrawableRes int getSelectedMiddleBackgroundRes();
    @DrawableRes int getSelectedEndBackgroundRes();
    @DrawableRes int getSelectedBeginBackgroundRes();
    @DrawableRes int getSelectedSingleBackgroundRes();
    void onDayClick(CalendarDay calendarDay, DaysAdapter.DayHolder holder, DaysAdapter adapter);
    List<DayDecorator> getDayDecorators();
}
