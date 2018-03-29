package com.edwardstock.vcalendar.decorators;

import android.content.Context;
import android.support.annotation.ColorRes;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.R;
import com.edwardstock.vcalendar.adapter.DayViewFacade;
import com.edwardstock.vcalendar.common.Consumer;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DisabledDayDecorator implements DayDecorator {
    private final Consumer<List<DateTime>> mDisabledDays;
    private final WeakReference<Context> mContext;
    private int mDisabledColor = R.color.vcal_text_color_half;

    public DisabledDayDecorator(Context context, Consumer<List<DateTime>> disabledDays, @ColorRes int colorRes) {
        this(context, disabledDays);
        mDisabledColor = colorRes;
    }

    public DisabledDayDecorator(Context context, Consumer<List<DateTime>> disabledDays) {
        mContext = new WeakReference<>(context);
        mDisabledDays = disabledDays;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return Stream.of(mDisabledDays.get())
                .map(item -> item.withTime(0, 0, 0, 0))
                .filter(item -> item.equals(calendarDay.getDateTime()))
                .count() > 0;
    }

    @Override
    public void decorate(CalendarDay calendarDay, DayViewFacade dayView, int neighbourhood) {
        dayView.setTextColorRes(mDisabledColor);
    }


}
