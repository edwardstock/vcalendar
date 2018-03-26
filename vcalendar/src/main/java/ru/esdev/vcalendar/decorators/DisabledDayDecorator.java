package ru.esdev.vcalendar.decorators;

import android.content.Context;
import android.widget.TextView;

import com.annimon.stream.Stream;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.List;

import ru.esdev.vcalendar.DayDecorator;
import ru.esdev.vcalendar.R;
import ru.esdev.vcalendar.common.Consumer;
import ru.esdev.vcalendar.models.CalendarDay;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DisabledDayDecorator implements DayDecorator {
    private final Consumer<List<DateTime>> mDisabledDays;
    private final WeakReference<Context> mContext;

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
    public void decorate(CalendarDay calendarDay, TextView dayView) {
        dayView.setTextColor(mContext.get().getResources().getColor(R.color.vcalendarTextColorHalf));
    }
}
