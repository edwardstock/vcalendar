package com.edwardstock.vcalendar.decorators;

import com.edwardstock.vcalendar.adapter.DayViewFacade;
import com.edwardstock.vcalendar.models.CalendarDay;

import androidx.annotation.DrawableRes;

import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_BEGIN;
import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_END;
import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_MIDDLE;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public abstract class ConnectedDayDecorator implements DayDecorator {
    @Override
    public void decorate(CalendarDay day, DayViewFacade tv, int neighbourhood) {
        if (neighbourhood == IS_BEGIN) {
            tv.setBackgroundResource(getSelectedBeginBackgroundRes());
        } else if (neighbourhood == IS_MIDDLE) {
            tv.setBackgroundResource(getSelectedMiddleBackgroundRes());
        } else if (neighbourhood == IS_END) {
            tv.setBackgroundResource(getSelectedEndBackgroundRes());
        } else {
            tv.setBackgroundResource(getSelectedSingleBackgroundRes());
        }
    }

    @DrawableRes
    public abstract int getSelectedBeginBackgroundRes();

    @DrawableRes
    public abstract int getSelectedMiddleBackgroundRes();

    @DrawableRes
    public abstract int getSelectedEndBackgroundRes();

    @DrawableRes
    public abstract int getSelectedSingleBackgroundRes();
}
