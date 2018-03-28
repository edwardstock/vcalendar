package com.edwardstock.vcalendar.adapter;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.CalendarHandler;
import com.edwardstock.vcalendar.R;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.models.CalendarDay;
import com.edwardstock.vcalendar.widgets.SquareTextView;

import org.joda.time.DateTime;
import org.joda.time.YearMonth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Map;

import timber.log.Timber;

import static com.edwardstock.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_BEGIN;
import static com.edwardstock.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_END;
import static com.edwardstock.vcalendar.adapter.DaysAdapter.Neighbourhood.IS_MIDDLE;
import static com.edwardstock.vcalendar.adapter.DaysAdapter.Neighbourhood.NO_NEIGHBOURS;
import static com.edwardstock.vcalendar.common.Preconditions.checkNotNull;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.WeekHolder> {
    private final CalendarDay[][] mData;
    private final YearMonth mMonth;
    private LayoutInflater mInflater;
    private WeakReference<Context> mContext;
    private WeakReference<CalendarHandler> mCalendarHandler;
    private DayItemClickedListener mDayItemClickedListener;
    private Map<Integer, DayIndex> mDayIndex = new ArrayMap<>();

    public DaysAdapter(@NonNull CalendarHandler calendarHandler, @NonNull final CalendarDay[][] weeksDays, YearMonth month) {
        mCalendarHandler = new WeakReference<>(checkNotNull(calendarHandler, "Calendar delegate can't be null"));
        mData = checkNotNull(weeksDays, "Days can't be null");
        mMonth = month;
    }

    public void setOnDayItemClickListener(DayItemClickedListener listener) {
        mDayItemClickedListener = listener;
    }

    @NonNull
    @Override
    public WeekHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        if (!isValidContext()) {
            mContext = new WeakReference<>(parent.getContext());
        }

        View view = mInflater.inflate(R.layout.item_week, parent, false);
        WeekHolder holder = new WeekHolder(view);

        if (mDayItemClickedListener != null) {
            Stream.of(holder.days).forEachIndexed((idx, item) -> {
                item.setOnClickListener(v -> {
                    final int pos = holder.getAdapterPosition();
                    if (mData[pos] != null && mData[pos][idx] != null) {
                        mDayItemClickedListener.onClick(mData[pos][idx], v, this);
                    }
                });
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeekHolder holder, int position) {
        final CalendarDay[] days = mData[position];
        Timber.d("Bind week %d in month %s", position + 1, mMonth.toString());

        Stream.of(days).forEachIndexed((idx, calendarDay) -> {
            final TextView tv = holder.days[idx];
            if (days[idx] == null) {
                tv.setVisibility(View.INVISIBLE);
                tv.setClickable(false);
                return;
            }

            tv.setVisibility(View.VISIBLE);
            tv.setClickable(true);
            tv.setText(String.valueOf(calendarDay.getDay()));

            resolveStyle(calendarDay, tv);

            Stream.of(mCalendarHandler.get().getDayDecorators()).filter(item -> item != null && item.shouldDecorate(calendarDay)).forEach(item -> item.decorate(calendarDay, tv));
        });
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public void updateRange(CalendarDay begin, CalendarDay end) {
        updateRange(begin.getDateTime(), end.getDateTime());
    }

    public void updateRange(DateTime begin, DateTime end) {
        if (begin.getMonthOfYear() != end.getMonthOfYear()) {
            Timber.w("Can't update range in a few months");
            return;
        }

        DayIndex first = getOrCreateIndex(begin.getDayOfMonth());
        DayIndex last = getOrCreateIndex(begin.getDayOfMonth());
        notifyItemRangeChanged(first.weekIdx, last.weekIdx - first.weekIdx);

    }

    public void update(CalendarDay calendarDay) {
        update(calendarDay.getDateTime());
    }

    public void update(DateTime dateTime) {
        DayIndex index = getOrCreateIndex(dateTime.getDayOfMonth());
        notifyItemChanged(index.weekIdx, index);

    }

    public boolean hasIndex(int dayNum) {
        return mDayIndex.containsKey(dayNum);
    }

    public DayIndex getIndex(int dayNum) {
        return mDayIndex.get(dayNum);
    }

    public DayIndex getOrCreateIndex(int dayNum) {
        if (!hasIndex(dayNum)) {
            return createIndex(dayNum);
        }

        return getIndex(dayNum);
    }

    private DayIndex createIndex(int dayNum) {
        int closerWeek = (int) ((float) dayNum / 7f);

        DayIndex index = null;

        if (mData[closerWeek][0] != null && mData[closerWeek][0].getDay() == dayNum) {
            index = new DayIndex(closerWeek, 0);
        } else if (mData[closerWeek][6] != null && mData[closerWeek][6].getDay() == dayNum) {
            index = new DayIndex(closerWeek, 6);
        } else {
            while (index == null && closerWeek < mData.length) {
                for (int i = 0; i < 7; i++) {
                    if (mData[closerWeek].length > i && mData[closerWeek][i] != null && mData[closerWeek][i].getDay() == dayNum) {
                        index = new DayIndex(closerWeek, i);
                        break;
                    }
                }
                closerWeek++;
            }
        }

        //noinspection ConstantConditions
        if (index == null) {
            throw new RuntimeException(String.format("Unable to find day %d in month %s", dayNum, mMonth.toString()));
        }

        mDayIndex.put(dayNum, index);

        return index;
    }

    private void putIndex(int dayNum, int weekIdx, int dayIdx) {
        mDayIndex.put(dayNum, new DayIndex(weekIdx, dayIdx));
    }

    private CalendarDay getDayByIndex(DayIndex index) {
        return mData[index.weekIdx][index.dayIdx];
    }

    private CalendarDay findDay(int dayNum) {
        return getDayByIndex(getOrCreateIndex(dayNum));
    }

    private boolean isValidContext() {
        return mContext != null && mContext.get() != null;
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

    private boolean hasNoNeighbours(@Nullable CalendarDay previousDay, @Nullable CalendarDay nextDay) {
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

    private void resolveStyle(CalendarDay day, TextView tv) {
        int neighbourhood;

        CalendarDay previousDay = mCalendarHandler.get().getPreviousDay(day);
        CalendarDay nextDay = mCalendarHandler.get().getNextDay(day);
        int dayOfWeek = day.getDateTime().getDayOfWeek();

        if ((nextDay.getMonth() > day.getMonth() || nextDay.getYear() > day.getYear()) && dayOfWeek != 7) {
            nextDay = null;
        }
        if ((previousDay.getMonth() < day.getMonth() || previousDay.getYear() < day.getYear()) && dayOfWeek != 1) {
            previousDay = null;
        }

        neighbourhood = getNeighbourhood(previousDay, nextDay);
        tv.setTextColor(mContext.get().getResources().getColor(R.color.vcal_text_color_dark));

        if (mCalendarHandler.get().getSelectionDispatcher().getMode() == SelectionMode.RANGE) {
            if (day.isSelected()) {
                if (neighbourhood == IS_BEGIN) {
                    tv.setBackgroundResource(mCalendarHandler.get().getSelectedBeginBackgroundRes());
                } else if (neighbourhood == IS_MIDDLE) {
                    tv.setBackgroundResource(mCalendarHandler.get().getSelectedMiddleBackgroundRes());
                } else if (neighbourhood == IS_END) {
                    tv.setBackgroundResource(mCalendarHandler.get().getSelectedEndBackgroundRes());
                } else {
                    tv.setBackgroundResource(mCalendarHandler.get().getSelectedSingleBackgroundRes());
                }
            } else {
                tv.setBackground(null);
            }
        } else {
            if (day.isSelected()) {
                tv.setBackgroundResource(mCalendarHandler.get().getSelectedSingleBackgroundRes());
            } else {
                tv.setBackground(null);
            }
        }
    }

    public interface DayItemClickedListener {
        void onClick(CalendarDay calendarDay, View dayView, DaysAdapter adapter);
    }

    @IntDef({NO_NEIGHBOURS, IS_BEGIN, IS_MIDDLE, IS_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Neighbourhood {
        int NO_NEIGHBOURS = 0;
        int IS_BEGIN = 1;
        int IS_MIDDLE = 2;
        int IS_END = 3;
    }

    public static final class DayIndex {
        int weekIdx;
        int dayIdx;

        DayIndex(int w, int d) {
            weekIdx = w;
            dayIdx = d;
        }

        public int getDayIdx() {
            return dayIdx;
        }

        public int getWeekIdx() {
            return weekIdx;
        }

        @Override
        public String toString() {
            return String.format("DayIndex{weekIdx=%d, dayIdx=%d}", weekIdx, dayIdx);
        }
    }

    public static class WeekHolder extends RecyclerView.ViewHolder {
        TextView[] days = new TextView[7];

        public WeekHolder(View itemView) {
            super(itemView);
            final ViewGroup vg = ((ViewGroup) itemView);
            days[0] = ((SquareTextView) vg.getChildAt(0));
            days[1] = ((SquareTextView) vg.getChildAt(1));
            days[2] = ((SquareTextView) vg.getChildAt(2));
            days[3] = ((SquareTextView) vg.getChildAt(3));
            days[4] = ((SquareTextView) vg.getChildAt(4));
            days[5] = ((SquareTextView) vg.getChildAt(5));
            days[6] = ((SquareTextView) vg.getChildAt(6));
        }
    }
}
