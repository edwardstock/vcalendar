package com.edwardstock.vcalendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.edwardstock.vcalendar.CalendarHandler;
import com.edwardstock.vcalendar.R;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;
import org.joda.time.YearMonth;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class CalendarMonthItem implements MultiRowContract.Row<CalendarMonthItem.ViewHolder> {
    private static final String[] MONTHS = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",};
    private final DaysAdapter mDaysAdapter;
    private final YearMonth mMonth;
    private final WeakReference<CalendarHandler> mHandler;
    private OnBindListener mOnBindListener;
    private OnUnbindListener mOnUnbindListener;

    public CalendarMonthItem(CalendarHandler calendarHandler, DateTime month, DaysAdapter.DayItemClickedListener dayItemClickedListener) {
        mMonth = new YearMonth(month.withTime(0, 0, 0, 0).dayOfMonth().withMinimumValue());
        mHandler = new WeakReference<>(calendarHandler);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month.getMonthOfYear() - 1);
        cal.set(Calendar.YEAR, month.getYear());
        cal.set(Calendar.DAY_OF_MONTH, month.dayOfMonth().withMaximumValue().getDayOfMonth());
        int weeksInMonth = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);


        CalendarDay[][] weeksDays = new CalendarDay[weeksInMonth][7];


        for (int weekNum = 0, dayNum = 1; weekNum < weeksInMonth; weekNum++) {
            CalendarDay[] days = weeksDays[weekNum];
            if (days == null) {
                weeksDays[weekNum] = new CalendarDay[7];
                days = weeksDays[weekNum];
            }

            for (int dayIdx = 0; dayIdx < 7; dayIdx++, dayNum++) {
                if (dayNum > month.dayOfMonth().withMaximumValue().getDayOfMonth()) {
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

        mDaysAdapter = new DaysAdapter(calendarHandler, weeksDays);
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
        return String.format("CalendarMonthItem{month=%s, weeks=%d}", mMonth.toString(), mDaysAdapter.getItemCount());
    }

    public CalendarMonthItem setLifecycle(OnBindListener bindListener, OnUnbindListener unbindListener) {
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
        return Integer.parseInt(String.format("%04d%02d", mMonth.getYear(), mMonth.getMonthOfYear()));
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
            viewHolder.monthName.setVisibility(mHandler.get().isEnabledLegend() ? View.VISIBLE : View.GONE);
        }

        viewHolder.monthName.setText(String.format("%s %d", MONTHS[mMonth.getMonthOfYear() - 1], mMonth.getYear()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        viewHolder.list.setLayoutManager(layoutManager);
        viewHolder.list.setNestedScrollingEnabled(false);
        viewHolder.list.setHasFixedSize(true);
        viewHolder.list.setItemViewCacheSize(mDaysAdapter.getItemCount());
        viewHolder.list.setDrawingCacheEnabled(true);
        viewHolder.list.setAdapter(mDaysAdapter);
        viewHolder.list.setItemAnimator(null);

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

    private boolean isValidHandler() {
        return mHandler != null && mHandler.get() != null;
    }

    public interface OnBindListener {
        void onBindMonth(YearMonth month);
    }

    public interface OnUnbindListener {
        void onUnbindMonth(YearMonth month);
    }

    public static class ViewHolder extends MultiRowAdapter.RowViewHolder {
        RecyclerView list;
        TextView monthName;

        public ViewHolder(View itemView) {
            super(itemView);
            monthName = itemView.findViewById(R.id.monthName);
            list = itemView.findViewById(R.id.list);
        }
    }
}
