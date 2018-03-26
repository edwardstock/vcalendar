package ru.esdev.vcalendar.adapter;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import ru.esdev.vcalendar.CalendarHandler;
import ru.esdev.vcalendar.R;
import ru.esdev.vcalendar.models.CalendarDay;

import static ru.esdev.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_BEGIN;
import static ru.esdev.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_END;
import static ru.esdev.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_MIDDLE;
import static ru.esdev.vcalendar.adapter.DaysAdapter.Neighbourhood.NO_NEIGHBOURS;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayHolder> {
    private final SparseArray<CalendarDay> mData;
    private LayoutInflater mInflater;
    private int mMaxCount;
    private int mOffset;
    private WeakReference<CalendarHandler> mCalendarHandler;

    public DaysAdapter(CalendarHandler calendarHandler, SparseArray<CalendarDay> days, int maxCount,
                       int offset) {
        mCalendarHandler = new WeakReference<>(calendarHandler);
        mData = days;
        mMaxCount = maxCount;
        mOffset = offset;
    }

    public void update(CalendarDay day) {
    	update(day.getDateTime());
    }

    public void update(DateTime day) {
        int dayIdx = day.getDayOfMonth();
        notifyItemChanged(dayIdx + mOffset);
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        View view = mInflater.inflate(R.layout.item_day, parent, false);
        DayHolder holder = new DayHolder(view);
        view.setOnClickListener(v -> mCalendarHandler.get().onDayClick(mData.get(holder.getAdapterPosition()), holder, this));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        CalendarDay calendarDay = mData.get(position);
        if (calendarDay != null) {
            holder.dayNum.setText(String.valueOf(calendarDay.getDay()));
            holder.dayNum.setVisibility(View.VISIBLE);
            holder.dayNum.setClickable(true);
            holder.dayNum.setFocusable(true);
            resolveStyle(calendarDay, holder);

	        Stream.of(mCalendarHandler.get().getDayDecorators())
			        .filter(item -> item != null && item.shouldDecorate(calendarDay))
			        .forEach(item -> item.decorate(calendarDay, holder.dayNum));
        } else {
            holder.dayNum.setVisibility(View.INVISIBLE);
            holder.dayNum.setClickable(false);
            holder.dayNum.setFocusable(false);
        }
    }

    @Override
    public int getItemCount() {
        return mMaxCount;
    }

    private boolean isEnd(CalendarDay nextDay) {
        return !isSelected(nextDay);
    }

    private boolean isMiddle(CalendarDay previousDay, CalendarDay nextDay) {
        return isSelected(previousDay) && isSelected(nextDay);
    }

    private boolean isBeginning(@Nullable CalendarDay previousDay) {
        return !isSelected(previousDay);
    }

    private boolean hasNoNeighbours(@Nullable CalendarDay previousDay,
                                    @Nullable CalendarDay nextDay) {
        return !isSelected(previousDay) && !isSelected(nextDay);
    }

    private boolean isSelected(@Nullable CalendarDay day) {
        return day != null && day.isSelected();
    }

    private int getNeighbourhood(CalendarDay previousDay, CalendarDay nextDay) {
        if (hasNoNeighbours(previousDay, nextDay)) {
            return NO_NEIGHBOURS;
        } else if (isBeginning(previousDay)) {
            return IS_BEGIN;
        } else if (isMiddle(previousDay, nextDay)) {
            return IS_MIDDLE;
        } else if (isEnd(nextDay)) {
            return IS_END;
        }

        return NO_NEIGHBOURS;
    }

    private void resolveStyle(CalendarDay day, DayHolder vh) {
        int neighbourhood;

        final Context context = vh.itemView.getContext();
        CalendarDay previousDay = mCalendarHandler.get().getPreviousDay(day);
        CalendarDay nextDay = mCalendarHandler.get().getNextDay(day);
	    int dayOfWeek = day.getDateTime().getDayOfWeek();

        if((nextDay.getMonth() > day.getMonth() || nextDay.getYear() > day.getYear()) && dayOfWeek != 7) {
            nextDay = null;
        }
        if((previousDay.getMonth() < day.getMonth() || previousDay.getYear() < day.getYear()) && dayOfWeek != 1) {
            previousDay = null;
        }

        final TextView tv = vh.dayNum;

        neighbourhood = getNeighbourhood(previousDay, nextDay);
        tv.setTextColor(context.getResources().getColor(R.color.vcalendarTextColorBlack));

        if (day.isSelected() && vh.dayNum.getBackground() == null) {
            if (neighbourhood == IS_BEGIN) {
                tv.setBackgroundResource(
                        mCalendarHandler.get().getSelectedBeginBackgroundRes());
            } else if (neighbourhood == IS_MIDDLE) {
                tv.setBackgroundResource(
                        mCalendarHandler.get().getSelectedMiddleBackgroundRes());
            } else if (neighbourhood == IS_END) {
                tv.setBackgroundResource(mCalendarHandler.get().getSelectedEndBackgroundRes());
            } else {
                tv.setBackgroundResource(
                        mCalendarHandler.get().getSelectedSingleBackgroundRes());
            }
        } else {
            tv.setBackground(null);
        }
    }

    @IntDef({
            NO_NEIGHBOURS,
            IS_BEGIN,
            IS_MIDDLE,
            IS_END
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Neighbourhood {
        int NO_NEIGHBOURS = 0;
        int IS_BEGIN = 1;
        int IS_MIDDLE = 2;
        int IS_END = 3;
    }

    public static class DayHolder extends RecyclerView.ViewHolder {
        TextView dayNum;

        public DayHolder(View itemView) {
            super(itemView);
            dayNum = itemView.findViewById(R.id.dayNum);
        }
    }
}
