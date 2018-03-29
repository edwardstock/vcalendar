package com.edwardstock.vcalendar;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.edwardstock.vcalendar.decorators.DayDecorator;
import com.edwardstock.vcalendar.handlers.SelectionDispatcher;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.util.Set;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface CalendarHandler {

    CalendarDay getDay(DateTime dateTime);
    CalendarDay getDayOrCreate(DateTime dateTime);
    @NonNull CalendarDay getPreviousDay(CalendarDay current);
    @NonNull CalendarDay getNextDay(CalendarDay current);
    @DrawableRes int getSelectedMiddleBackgroundRes();
    @DrawableRes int getSelectedEndBackgroundRes();
    @DrawableRes int getSelectedBeginBackgroundRes();
    @DrawableRes int getSelectedSingleBackgroundRes();
    Set<DayDecorator> getDayDecorators();
    SelectionDispatcher getSelectionDispatcher();
    boolean isEnabledLegend();
    String[] getMonthNames();
    String[] getDaysOfWeek();
}
