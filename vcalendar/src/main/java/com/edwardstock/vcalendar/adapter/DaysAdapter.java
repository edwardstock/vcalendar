package com.edwardstock.vcalendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.CalendarHandler;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.decorators.DefaultDayDecorator;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_BEGIN;
import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_END;
import static com.edwardstock.vcalendar.adapter.Neighbourhood.IS_MIDDLE;
import static com.edwardstock.vcalendar.adapter.Neighbourhood.NO_NEIGHBOURS;
import static com.edwardstock.vcalendar.common.Preconditions.checkNotNull;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.WeekHolder> {
    private final CalendarDay[][] mData;
    private final DefaultDayDecorator mDefaultDayDecorator = new DefaultDayDecorator();
    private LayoutInflater mInflater;
    private WeakReference<Context> mContext;
    private final WeakReference<CalendarHandler> mCalendarHandler;
    private DayItemClickedListener mDayItemClickedListener;
    private final Map<Integer, DayIndex> mDayIndex = new ArrayMap<>();

    public DaysAdapter(@NonNull CalendarHandler calendarHandler,
                       @NonNull final CalendarDay[][] weeksDays) {
        mCalendarHandler = new WeakReference<>(checkNotNull(calendarHandler, "Calendar delegate can't be null"));
        mData = checkNotNull(weeksDays, "Days can't be null");
        if (mData.length == 0) {
            throw new IllegalArgumentException("Days can't be empty!");
        }
    }

    public void setOnDayItemClickListener(DayItemClickedListener listener) {
        mDayItemClickedListener = listener;
    }

    @NonNull
    @Override
    public WeekHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (!isValidContext()) {
            mContext = new WeakReference<>(parent.getContext());
        }
        if (mInflater == null) {
            mInflater = LayoutInflater.from(mContext.get());
        }

        View view = mInflater.inflate(mCalendarHandler.get().getWeekLayoutRes(), parent, false);
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
        if (index == null) {
            Timber.i("Day not found: %s", dateTime.toString());
            return;
        }
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
        // wtf ask you? ok, so try to find 7 day in first week.
        // if we just divide 7/7 than we will have 1, first week?
        // yes, but index of first week must be 0,
        // so 14 day will be 3rd week, not the second, but 14 day is a sunday
        // and it can't be in the next week..
        int closerWeek = (int) (((float) dayNum - 0.1f) / 7f);

        DayIndex index = null;

        if (closerWeek > mData.length) {
            closerWeek = 0;
        }

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
            return null;
        }

        mDayIndex.put(dayNum, index);

        return index;
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

    private void resolveStyle(final CalendarDay day, final TextView tv) {
        CalendarDay previousDay = mCalendarHandler.get().getPreviousDay(day);
        CalendarDay nextDay = mCalendarHandler.get().getNextDay(day);
        int dayOfWeek = day.getDateTime().getDayOfWeek();

        if ((nextDay.getMonth() > day.getMonth() || nextDay.getYear() > day.getYear()) && dayOfWeek != 7) {
            nextDay = null;
        }
        if ((previousDay.getMonth() < day.getMonth() || previousDay.getYear() < day.getYear()) && dayOfWeek != 1) {
            previousDay = null;
        }

        int neighbourhood = getNeighbourhood(previousDay, nextDay);
        final DayViewFacade facade = new DayViewFacade(tv);
        facade.reset();
        facade.setSelectedState(day.isSelected());

        boolean isRange = mCalendarHandler.get().getSelectionDispatcher().getMode() == SelectionMode.RANGE;
        if (mCalendarHandler.get().isEnabledDefaultDecorator()) {
            if (mDefaultDayDecorator.shouldDecorate(day)) {
                mDefaultDayDecorator.decorate(day, facade, neighbourhood);
            }
        }
        Stream.of(mCalendarHandler.get().getDayDecorators())
                .filter(item -> item.shouldDecorate(day))
                .forEach(item -> item.decorate(day, facade, isRange ? neighbourhood : Neighbourhood.NO_NEIGHBOURS));
    }

    public interface DayItemClickedListener {
        void onClick(CalendarDay calendarDay, View dayView, DaysAdapter adapter);
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

        @SuppressLint("DefaultLocale")
        @Override
        public String toString() {
            return String.format("DayIndex{weekIdx=%d, dayIdx=%d}", weekIdx, dayIdx);
        }
    }

    public static final class WeekHolder extends RecyclerView.ViewHolder {
        TextView[] days = new TextView[7];

        public WeekHolder(View itemView) {
            super(itemView);
            final ViewGroup vg = ((ViewGroup) itemView);
            if (vg.getChildCount() != 7) {
                throw new IllegalStateException(
                        "Invalid week layout passed to days adapter! Layout MUST contains exact 7 TextView children.");
            }
            days[0] = ((TextView) vg.getChildAt(0));
            days[1] = ((TextView) vg.getChildAt(1));
            days[2] = ((TextView) vg.getChildAt(2));
            days[3] = ((TextView) vg.getChildAt(3));
            days[4] = ((TextView) vg.getChildAt(4));
            days[5] = ((TextView) vg.getChildAt(5));
            days[6] = ((TextView) vg.getChildAt(6));
        }
    }
}
