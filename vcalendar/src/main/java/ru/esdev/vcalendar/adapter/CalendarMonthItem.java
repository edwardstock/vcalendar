package ru.esdev.vcalendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.Calendar;

import ru.esdev.vcalendar.CalendarHandler;
import ru.esdev.vcalendar.R;
import ru.esdev.vcalendar.models.CalendarDay;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class CalendarMonthItem implements MultiRowContract.Row<CalendarMonthItem.ViewHolder> {
	private static final String[] MONTHS = new String[]{
			"Январь",
			"Февраль",
			"Март",
			"Апрель",
			"Май",
			"Июнь",
			"Июль",
			"Август",
			"Сентябрь",
			"Октябрь",
			"Ноябрь",
			"Декабрь",
	};
	private DaysAdapter mDaysAdapter;
	private DateTime mMonth;

	public CalendarMonthItem(CalendarHandler calendarHandler, DateTime month) {
		DateTime lastDay = month.withTime(0, 0, 0, 0).dayOfMonth().withMaximumValue();
		mMonth = lastDay.dayOfMonth().withMinimumValue();
		int daysCount = lastDay.getDayOfMonth();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month.getMonthOfYear()-1);
		cal.set(Calendar.YEAR, month.getYear());
		cal.set(Calendar.DAY_OF_MONTH, month.getDayOfMonth());
		int weeks = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
		int weekMaxDays = weeks * 7;


		SparseArray<CalendarDay> days = new SparseArray<>(weekMaxDays);

		int offset = 0;
		for (int i = 0, num = 1; i < weekMaxDays; i++, num++) {
			if (num > month.dayOfMonth().withMaximumValue().getDayOfMonth()) {
				break;
			}

			DateTime dayDt = month.withDayOfMonth(num);
			if (i == 0) {
				offset = dayDt.getDayOfWeek();
				if (offset > num) {
					i = offset - 1;
				}
			}
			days.append(i, calendarHandler.getDayOrCreate(dayDt));

		}
		mDaysAdapter = new DaysAdapter(calendarHandler, days, weekMaxDays, offset);
		calendarHandler.setMonthRow(this, month);
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
		return String.format("CalendarMonthItem{month=%04d-%02d, days=%s}", mMonth.getYear(), mMonth.getMonthOfYear(), mDaysAdapter.getItemCount());
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

	@Override
	public boolean isVisible() {
		return true;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder) {
		Context context = viewHolder.itemView.getContext();

		viewHolder.monthName.setText(String.format("%s %d", MONTHS[mMonth.getMonthOfYear() -
				1], mMonth.getYear()));

//        mDaysAdapter.setHasStableIds(true);
		GridLayoutManager layoutManager = new NonScrollableGridLayoutManager(context, 7,
				GridLayoutManager.VERTICAL,
				false);

		viewHolder.list.setLayoutManager(layoutManager);
		viewHolder.list.setNestedScrollingEnabled(false);
		viewHolder.list.setHasFixedSize(true);
		viewHolder.list.setItemViewCacheSize(mDaysAdapter.getItemCount());
		viewHolder.list.setDrawingCacheEnabled(true);
		viewHolder.list.setAdapter(mDaysAdapter);
		viewHolder.list.setItemAnimator(null);
	}

	@Override
	public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

	}

	@NonNull
	@Override
	public Class<ViewHolder> getViewHolderClass() {
		return ViewHolder.class;
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
