package com.edwardstock.vcalendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edwardstock.vcalendar.CalendarHandler;
import com.edwardstock.vcalendar.R;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;
import org.joda.time.YearMonth;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class CalendarMonthItem implements CalendarAdapterContract.Row<CalendarMonthItem.ViewHolder> {
    private final DaysAdapter mDaysAdapter;
    private final YearMonth mMonth;
    private final WeakReference<CalendarHandler> mHandler;
    private OnBindListener mOnBindListener;
    private OnUnbindListener mOnUnbindListener;

    public CalendarMonthItem(CalendarHandler calendarHandler, @NonNull DateTime month,
                             DaysAdapter.DayItemClickedListener dayItemClickedListener) {
        mMonth = new YearMonth(month);
        mHandler = new WeakReference<>(calendarHandler);

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month.getMonthOfYear() - 1);
        cal.set(Calendar.YEAR, month.getYear());
        cal.set(Calendar.DAY_OF_MONTH, month.dayOfMonth().withMaximumValue().getDayOfMonth());
        int weeksInMonth = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);

        int fromWeek = 0;
        int toWeek = weeksInMonth;
        if (calendarHandler.hasMinDate() && calendarHandler.isMinDateCutable() && equalsMonthYear(
                calendarHandler.getMinDate(), month)) {
            cal.set(Calendar.DAY_OF_MONTH, calendarHandler.getMinDate().getDayOfMonth());
            fromWeek = cal.get(Calendar.WEEK_OF_MONTH);
            if (fromWeek > 0) {
                fromWeek--;
            }
        }
        if (calendarHandler.hasMaxDate() && calendarHandler.isMaxDateCutable() && equalsMonthYear(
                calendarHandler.getMaxDate(), month)) {
            cal.set(Calendar.DAY_OF_MONTH, calendarHandler.getMaxDate().getDayOfMonth());
            toWeek = cal.get(Calendar.WEEK_OF_MONTH);
            if (toWeek < weeksInMonth) {
                toWeek++;
            }
        }

        CalendarDay[][] weeksDaysWithOffset = new CalendarDay[weeksInMonth][7];

        int maxMonthDay = month.dayOfMonth().withMaximumValue().getDayOfMonth();
        for (int weekIdx = 0, dayNum = 1; weekIdx < weeksInMonth; weekIdx++) {
            CalendarDay[] days = weeksDaysWithOffset[weekIdx];
            if (days == null) {
                weeksDaysWithOffset[weekIdx] = new CalendarDay[7];
                days = weeksDaysWithOffset[weekIdx];
            }

            for (int dayIdx = 0; dayIdx < 7; dayIdx++, dayNum++) {
                if (dayNum > maxMonthDay) {
                    days[dayIdx] = null;
                    break;
                }

                DateTime dayDateTime = month.withDayOfMonth(dayNum);
                int dayOrder = dayDateTime.getDayOfWeek() - 1;
                if (dayOrder > dayIdx) {
                    dayIdx = dayOrder;
                }

                days[dayIdx] = calendarHandler.getDayOrCreate(dayDateTime);
            }
        }

        if (fromWeek > 0 || toWeek < weeksInMonth) {
            weeksDaysWithOffset = Arrays.copyOfRange(weeksDaysWithOffset, fromWeek, toWeek);
        }

        mDaysAdapter = new DaysAdapter(calendarHandler, weeksDaysWithOffset);
        mDaysAdapter.setOnDayItemClickListener(dayItemClickedListener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarMonthItem that = (CalendarMonthItem) o;
        return mMonth.equals(that.mMonth);
    }

    @Override
    public int hashCode() {
        return mMonth.hashCode();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("CalendarMonthItem{month=%s, weeks=%d}", mMonth.toString(),
                             mDaysAdapter.getItemCount());
    }

    public CalendarMonthItem setLifecycle(OnBindListener bindListener,
                                          OnUnbindListener unbindListener) {
        mOnBindListener = bindListener;
        mOnUnbindListener = unbindListener;
        return this;
    }

    public DaysAdapter getAdapter() {
        return mDaysAdapter;
    }

    @Override
    public int getItemView() {
        return R.layout.item_month;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int getRowPosition() {
        return Integer.parseInt(
                String.format("%04d%02d", mMonth.getYear(), mMonth.getMonthOfYear()));
    }

    public YearMonth getMonth() {
        return mMonth;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder) {
        Context context = viewHolder.itemView.getContext();

        if (isValidHandler()) {
            viewHolder.monthName.setVisibility(
                    mHandler.get().isEnabledLegend() ? View.VISIBLE : View.GONE);
            viewHolder.monthName.setText(String.format("%s %d",
                                                       mHandler.get().getMonthNames()[mMonth.getMonthOfYear() - 1],
                                                       mMonth.getYear()));

            for (int i = 0; i < 7; i++) {
                ((TextView) viewHolder.weekDaysLayout.getChildAt(i))
                        .setText(mHandler.get().getDaysOfWeek()[i]);
            }
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(context,
                                                                    LinearLayoutManager.VERTICAL,
                                                                    false);

        if (viewHolder.list.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) viewHolder.list.getItemAnimator()).setSupportsChangeAnimations(
                    false);
        }
        viewHolder.list.setLayoutManager(layoutManager);
        viewHolder.list.setNestedScrollingEnabled(false);
        viewHolder.list.setHasFixedSize(true);
        viewHolder.list.setItemViewCacheSize(mDaysAdapter.getItemCount());
        viewHolder.list.setDrawingCacheEnabled(true);
        viewHolder.list.setAdapter(mDaysAdapter);


        if (mOnBindListener != null) {
            mOnBindListener.onBindMonth(getMonth());
        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        if (mOnUnbindListener != null) {
            mOnUnbindListener.onUnbindMonth(getMonth());
        }
    }

    @NonNull
    @Override
    public Class<ViewHolder> getViewHolderClass() {
        return ViewHolder.class;
    }

    private boolean equalsMonthYear(DateTime first, DateTime second) {
        return first.getYear() == second.getYear() && first.getMonthOfYear() == second.getMonthOfYear();
    }

    private boolean isValidHandler() {
        return mHandler != null && mHandler.get() != null;
    }

    public interface OnBindListener {
        void onBindMonth(YearMonth month);
    }

    public interface OnUnbindListener {
        void onUnbindMonth(YearMonth month);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView list;
        TextView monthName;
        LinearLayout weekDaysLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            monthName = itemView.findViewById(R.id.monthName);
            list = itemView.findViewById(R.id.list);
            weekDaysLayout = itemView.findViewById(R.id.weekDaysLayout);
        }
    }
}
