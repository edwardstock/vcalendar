package com.edwardstock.vcalendar.handlers;

import com.edwardstock.vcalendar.OnDayClickListener;
import com.edwardstock.vcalendar.models.CalendarDay;

import java.util.List;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public abstract class BaseHandler implements OnDayClickListener {
    private SelectionDispatcher mFactory;

    public BaseHandler(SelectionDispatcher factory) {
        mFactory = factory;
    }

    public abstract void setSelections(List<CalendarDay> selections);

    SelectionDispatcher getDispatcher() {
        return mFactory;
    }
}
