package ru.esdev.vcalendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.annimon.stream.Stream;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.esdev.vcalendar.adapter.CalendarMonthItem;
import ru.esdev.vcalendar.adapter.DaysAdapter;
import ru.esdev.vcalendar.adapter.MultiRowAdapter;
import ru.esdev.vcalendar.decorators.DisabledRangeDayDecorator;
import ru.esdev.vcalendar.models.CalendarDay;
import timber.log.Timber;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@SuppressWarnings("Convert2MethodRef")
public class VCalendar extends FrameLayout implements CalendarHandler {

    public final static int SELECTION_NONE = 0;
    public final static int SELECTION_SINGLE = 1;
    public final static int SELECTION_MULTIPLE = 2;
    public final static int SELECTION_RANGE = 3;

    @DrawableRes
    private int mSelectedMiddleBackgroundRes;
    @DrawableRes
    private int mSelectedEndBackgroundRes;
    @DrawableRes
    private int mSelectedSingleBackgroundRes;
    @SelectionMode
    private int mSelectionMode = SELECTION_NONE;
    private int mOrientation = LinearLayoutManager.VERTICAL;
    @DrawableRes
    private int mSelectedBeginBackgroundRes;
    private List<CalendarDay> mSelections = new ArrayList<>();
    private RecyclerView mList;
    private MultiRowAdapter mAdapter;
    private Map<DateTime, CalendarDay> mDayMap = new HashMap<>();
    private int mFutureMonth = 0;
    private int mPastMonth = 0;
    private DateTime mInitial;
    private Map<DateTime, CalendarMonthItem> mRowMap = new HashMap<>();
    private short mSelectionsClickCount = 0;
    private int mSelectionsLimit = 0;
    private List<OnSelectionListener> mOnSelectionListeners = new ArrayList<>();
    private List<DayDecorator> mDayDecorators = new ArrayList<>();
    private List<OnDayClickListener> mOnDayClickListeners = new ArrayList<>();
    private DateTime mMinDate;
    private DateTime mMaxDate;
    private DateTime mDisabledBeforeDate;
    private DateTime mDisabledAfterDate;
    /**
     * Делаем все то же самое что и для range, только не добавляем в список промежуточные дни
     */
    private volatile OnDayClickListener mSelectionMultipleHandler = (dayView, calendarDay) -> {

        boolean cantSelect = checkDayIsBeforeOrAfterDisabled(calendarDay);

        if (cantSelect) {
            return;
        }

        // после 2го клика, сбрасываем выборку и выделяем последний выбранный день
        if (mSelections.contains(calendarDay)) {
            calendarDay.setSelected(false);
            mSelections.remove(calendarDay);
            return;
        }

        if (hasSelectionLimit() && mSelections.size() == mSelectionsLimit) {
            callOnSelectionListeners(mSelections, true);
            return;
        }

        calendarDay.setSelected(true);
        mSelections.add(calendarDay);

        // если более 1го дня выбрано, сортируем
        if (mSelections.size() > 1) {
            Collections.sort(mSelections);
        }

        callOnSelectionListeners(mSelections, false);
    };
    private volatile OnDayClickListener mSelectionSingleHandler = (dayView, calendarDay) -> {
        boolean cantSelect = checkDayIsBeforeOrAfterDisabled(calendarDay);

        if (cantSelect) {
            return;
        }
        clearSelectionsInternal();
        calendarDay.setSelected(true);
        mSelections.add(calendarDay);

        callOnSelectionListeners(mSelections, false);
    };
    private volatile OnDayClickListener mSelectionRangeHandler = (dayView, calendarDay) -> {
        boolean cantSelect = checkDayIsBeforeOrAfterDisabled(calendarDay);

        if (cantSelect) {
            return;
        }

        // после 2го клика, сбрасываем выборку и выделяем последний выбранный день
        if (mSelectionsClickCount == 2) {
            clearSelectionsInternal();
            mSelectionsClickCount = 0;
        }
        mSelectionsClickCount++;


        calendarDay.setSelected(true);
        mSelections.add(calendarDay);

        // если более 1го дня выбрано, сортируем
        if (mSelections.size() > 1) {
            Collections.sort(mSelections);
        }

        // если есть выбранные элементы
        if (mSelections.size() > 0) {
            final CalendarDay first = mSelections.get(0);
            final CalendarDay last = mSelections.get(mSelections.size() - 1);
            // если первый выбранный день не равен последнему, то есть если выбранно более 2х дней
            if (!first.equals(last)) {
                // считаем разницу в днях
                final Duration duration = first.getDiffDuration(last);
                long diff = duration.getStandardDays();

                // очищаем всю выборку
                clearSelectionsInternal();

                // записываем весь range дней по новой, для того чтобы проще было вставить промеждуточные дни
                for (int i = -1; i < diff; i++) {
                    // Joda time почти всегда immutable, поэтому делаем новые переменные

                    if (hasSelectionLimit() && i + 1 >= mSelectionsLimit) {
                        break;
                    }

                    // берем первый день
                    final DateTime cal = first.getDateTime();
                    // к нему добавляем итерацию + 1 день, таким образом получаем следующий день
                    // добавляем дни в LocalDate потому что в DateTime нельзя указать 50 день января,
                    // будет переполнение и ошибка, вместо этого используем LocalDate
                    // которому можно указать 50 января
                    final LocalDate localDate = new LocalDate(cal).plusDays(i + 1);

                    // переводим LocalDate в DateTime с аргументом предыдущего дня, чтобы посчиталось все относительно
                    final DateTime newCal = localDate.toDateTime(cal);

                    CalendarDay nextDay = getDayOrCreate(newCal);
                    nextDay.setSelected(true);
                    mSelections.add(nextDay);
                }
            }
        }

        // just in case
        if (mSelections.size() > 1) {
            Collections.sort(mSelections);
        }

        callOnSelectionListeners(mSelections, mSelections.size() == getSelectionLimit());
        updateSelections();
    };

    public VCalendar(@NonNull Context context) {
        super(context);
    }

    public VCalendar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public VCalendar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public VCalendar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    public static void initialize(Context context) {
        initialize(context, false);
    }

    public static void initialize(Context context, boolean debug) {
        JodaTimeAndroid.init(context);
        if (debug) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public void addDayDecorator(DayDecorator decorator) {
        mDayDecorators.add(decorator);
    }

    public void removeDayDecorator(DayDecorator decorator) {
        mDayDecorators.remove(decorator);
    }

    public void clearDayDecorators() {
        mDayDecorators.clear();
    }

    @Override
    public CalendarDay getDay(DateTime dateTime) {
        return mDayMap.get(dateTime.withTime(0, 0, 0, 0));
    }

    @Override
    public CalendarDay getDayOrCreate(DateTime dateTime) {
        final DateTime dt = dateTime.withTime(0, 0, 0, 0);
        if (getDay(dt) != null) {
            return getDay(dt);
        }

        CalendarDay cd = new CalendarDay(dt);
        mDayMap.put(dt, cd);
        return cd;
    }

    @Override
    public void setMonthRow(CalendarMonthItem calendarMonthItem, DateTime dateTime) {
        mRowMap.put(dateTime.dayOfMonth().withMinimumValue().withTime(0, 0, 0, 0),
                    calendarMonthItem);
    }

    @NonNull
    @Override
    public CalendarDay getPreviousDay(CalendarDay current) {
        return getDayOrCreate(current.getDateTime().minusDays(1));
    }

    @Override
    public int getSelectedMiddleBackgroundRes() {
        return mSelectedMiddleBackgroundRes;
    }

    @Override
    public int getSelectedEndBackgroundRes() {
        return mSelectedEndBackgroundRes;
    }

    @Override
    public int getSelectedBeginBackgroundRes() {
        return mSelectedBeginBackgroundRes;
    }

    @Override
    public int getSelectedSingleBackgroundRes() {
        return mSelectedSingleBackgroundRes;
    }

    public void addOnSelectionListener(OnSelectionListener listener) {
        mOnSelectionListeners.add(listener);
    }

    public void removeOnSelectionListener(OnSelectionListener listener) {
        mOnSelectionListeners.remove(listener);
    }

    public void clearOnSelectionListeners() {
        mOnSelectionListeners.clear();
    }

    public void addOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListeners.add(listener);
    }

    public void removeOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListeners.remove(listener);
    }

    public void clearOnDayClickListeners() {
        mOnDayClickListeners.clear();
    }

    @Override
    public void onDayClick(CalendarDay calendarDay, DaysAdapter.DayHolder holder,
                           DaysAdapter adapter) {

        switch (mSelectionMode) {
            case SELECTION_RANGE:
                mSelectionRangeHandler.onClick(holder.itemView, calendarDay);
                break;

            case SELECTION_MULTIPLE:
                mSelectionMultipleHandler.onClick(holder.itemView, calendarDay);
                break;
            case SELECTION_SINGLE:
                mSelectionSingleHandler.onClick(holder.itemView, calendarDay);
                break;

            case SELECTION_NONE:
                //do nonthing
                break;
        }

        Stream.of(mOnDayClickListeners)
                .filter(item -> item != null)
                .forEach(item -> item.onClick(holder.itemView, calendarDay));
    }

    @Override
    public List<DayDecorator> getDayDecorators() {
        return mDayDecorators;
    }

    @NonNull
    @Override
    public CalendarDay getNextDay(CalendarDay current) {
        return getDayOrCreate(current.getDateTime().plusDays(1));
    }

    public List<CalendarDay> getSelections() {
        return mSelections == null ? Collections.emptyList() : new ArrayList<>(mSelections);
    }

    public void setSelections(CalendarDay selections) {
        setSelections(Stream.of(selections).map(item -> item.getDateTime()).toList());
    }

    public void setSelections(List<DateTime> selections) {
        if (selections == null || selections.size() == 0 || mSelectionMode == SELECTION_NONE) {
            return;
        }

        if (mSelections == null) {
            mSelections = new ArrayList<>(20);
        }

        final List<DateTime> valid = Stream.of(selections)
                .filter(item -> item != null)
                .toList();

        if (valid.size() == 2 && valid.get(0).equals(valid.get(1))) {
            selections.remove(1);
        }

        if (mSelectionMode == SELECTION_SINGLE) {
            mSelections.add(getDay(valid.get(0)).setSelected(true));
        } else if (mSelectionMode == SELECTION_MULTIPLE) {
            mSelections = Stream.of(selections)
                    .filter(item -> item != null)
                    .map(this::getDayOrCreate)
                    .map(item -> item.setSelected(true))
                    .toList();
        } else if (mSelectionMode == SELECTION_RANGE) {
            mSelections = Stream.of(selections)
                    .filter(item -> item != null)
                    .map(this::getDayOrCreate)
                    .map(item -> item.setSelected(true))
                    .toList();

            // считаем разницу в днях
            final CalendarDay first = mSelections.get(0);
            final CalendarDay last = mSelections.get(mSelections.size() - 1);
            final Duration duration = first.getDiffDuration(last);
            long diff = duration.getStandardDays();

            // очищаем всю выборку
            clearSelectionsInternal();

            // записываем весь range дней по новой, для того чтобы проще было вставить промеждуточные дни
            for (int i = -1; i < diff; i++) {
                // Joda time почти всегда immutable, поэтому делаем новые переменные
                if (hasSelectionLimit() && i + 1 >= mSelectionsLimit) {
                    break;
                }

                // берем первый день
                final DateTime cal = first.getDateTime();
                // к нему добавляем итерацию + 1 день, таким образом получаем следующий день
                // добавляем дни в LocalDate потому что в DateTime нельзя указать 50 день января,
                // будет переполнение и ошибка, вместо этого используем LocalDate
                // которому можно указать 50 января
                final LocalDate localDate = new LocalDate(cal).plusDays(i + 1);

                // переводим LocalDate в DateTime с аргументом предыдущего дня, чтобы посчиталось все относительно
                final DateTime newCal = localDate.toDateTime(cal);

                CalendarDay nextDay = getDayOrCreate(newCal);
                mSelections.add(nextDay.setSelected(true));
            }
        }

        mSelectionsClickCount = 2;// make next click as new selection
        updateSelections();

        if (mSelections.size() > 0) {
            CalendarDay firstSelected = mSelections.get(0);
//			int lCnt = yearMonth(firstSelected);
//			int rCnt = yearMonth(mInitial);
            int diff = Months.monthsBetween(mInitial, firstSelected.getDateTime()).getMonths();

            if (diff < 0) {
                diff *= -1;
                if (diff > mPastMonth) {
                    drawMonthPast(diff - mPastMonth, true);
                }
            } else if (diff > 0) {
                if (diff > mFutureMonth) {
                    drawMonthFuture(diff - mFutureMonth, true);
                }
            }
//			if(lCnt > rCnt && mFutureMonth < lCnt) {
//				drawMonthFuture((lCnt - rCnt)+1, true);
//			} else if(lCnt < rCnt && mPastMonth < lCnt) {
//				drawMonthPast((lCnt - rCnt)+1, true);
//			}
        }

    }

    public boolean hasSelectionLimit() {
        return mSelectionsLimit > 0;
    }

    public boolean hasSelections() {
        return mSelectionMode != SELECTION_NONE && !getSelections().isEmpty();
    }

    public int getSelectionLimit() {
        return mSelectionsLimit;
    }

    public void setSelectionDisabledBefore(DateTime dateTime) {
        mDisabledBeforeDate = dateTime.minusDays(1);
        addDayDecorator(new DisabledRangeDayDecorator(getContext(), DisabledRangeDayDecorator.RangeMode.BEFORE, dateTime));
    }

    public void setSelectionDisabledBefore(Date date) {
        setSelectionDisabledBefore(new DateTime(date));
    }

    public void setSelectionDisabledAfter(DateTime dateTime) {
        mDisabledAfterDate = dateTime.plusDays(1);
        addDayDecorator(new DisabledRangeDayDecorator(getContext(), DisabledRangeDayDecorator.RangeMode.AFTER, dateTime));
    }

    public void setSelectionDisabledAfter(Date date) {
        setSelectionDisabledAfter(new DateTime(date));
    }

    public void setMinDate(Date date) {
        setMinDate(new DateTime(date));
    }

    public void setMinDate(DateTime minDate) {
        mMinDate = minDate;
    }

    public void setMaxDate(Date date) {
        setMaxDate(new DateTime(date));
    }

    public void setMaxDate(DateTime maxDate) {
        mMaxDate = maxDate;
    }

    private boolean checkDayIsBeforeOrAfterDisabled(CalendarDay calendarDay) {
        if (mDisabledAfterDate == null && mDisabledBeforeDate == null) {
            return false;
        }

        if (mDisabledBeforeDate != null &&
                calendarDay.getDateTime().compareTo(mDisabledBeforeDate) < 0) {
            return true;
        }

        if (mDisabledAfterDate != null &&
                calendarDay.getDateTime().compareTo(mDisabledAfterDate) > 0) {
            return true;
        }

        return false;
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(getContext(), R.layout.vcalendar_main, this);
        TypedArray def = getContext()
                .obtainStyledAttributes(attrs, R.styleable.VCalendar, defStyleAttr, defStyleRes);

        mSelectedBeginBackgroundRes = def.getResourceId(
                R.styleable.VCalendar_selectionBeginBackground, 0);
        mSelectedMiddleBackgroundRes = def.getResourceId(
                R.styleable.VCalendar_selectionMiddleBackground, 0);
        mSelectedEndBackgroundRes = def.getResourceId(R.styleable.VCalendar_selectionEndBackground,
                                                      0);
        mSelectedSingleBackgroundRes = def.getResourceId(
                R.styleable.VCalendar_selectionSingleBackground, 0);
        mOrientation = def.getInt(R.styleable.VCalendar_orientation, LinearLayoutManager.VERTICAL);
        mSelectionMode = def.getInt(R.styleable.VCalendar_selectionMode, SELECTION_NONE);

        def.recycle();

        mInitial = new DateTime().withTime(0, 0, 0, 0);

        mList = findViewById(R.id.mainList);
        mAdapter = new MultiRowAdapter();
        mAdapter.setEnableSorting(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                                                                          mOrientation, false);
        mList.setLayoutManager(layoutManager);


        DateTime lastDay = mInitial.dayOfMonth().withMaximumValue();

        CalendarMonthItem calendarMonthItem = new CalendarMonthItem(this, lastDay);
        mAdapter.addRow(calendarMonthItem);
        mList.setAdapter(mAdapter);
        mList.setItemViewCacheSize(10);
        mList.setDrawingCacheEnabled(true);
        mList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int firstH = mList.getChildAt(0).getHeight();
                int toDrawCount = (int) Math.ceil((float) getHeight() / (float) firstH) + 1;
                drawMonthFuture(toDrawCount);

                mList.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                if (lastVisible + 1 >= mAdapter.getItemCount()) {
                    drawMonthFuture(3);
                } else if (firstVisible <= 1) {
//					drawMonthPast();
                }
//				Timber.d("First visible: %d, last: %d", firstVisible, lastVisible);
            }
        });
    }

    private int yearMonth(CalendarDay cd) {
        return yearMonth(cd.getDateTime());
    }

    @SuppressLint("DefaultLocale")
    private int yearMonth(DateTime dt) {
        return Integer.parseInt(String.format("%04d%02d", dt.getYear(), dt.getMonthOfYear()));
    }

    private void callOnSelectionListeners(List<CalendarDay> selections, boolean isLimitExceeded) {
        for (OnSelectionListener l : mOnSelectionListeners) {
            l.onSelected(selections, isLimitExceeded);
        }
    }

    private void clearSelectionsInternal() {
        Set<DateTime> months = new HashSet<>();
        for (CalendarDay day : mSelections) {
            DateTime month = day.getDateTime().dayOfMonth().withMinimumValue();
            months.add(month);
            day.setSelected(false);
        }

        for (DateTime month : months) {
            if (mRowMap.containsKey(month)) {
                mRowMap.get(month).getAdapter().notifyDataSetChanged();
            }
        }

        mSelections.clear();
    }

    private void updateSelections() {
        Set<DateTime> months = new HashSet<>();
        for (CalendarDay day : mSelections) {
            DateTime month = day.getDateTime().dayOfMonth().withMinimumValue();
            months.add(month);
        }

        for (DateTime month : months) {
            if (mRowMap.containsKey(month)) {
                // update only selected items
                mRowMap.get(month).getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void drawMonthFuture(int cnt, boolean scrollToLast) {
        mList.post(() -> {
            final CalendarMonthItem[] rows = new CalendarMonthItem[cnt];
            for (int i = 0; i < cnt; i++) {
                DateTime current = mInitial.plusMonths(++mFutureMonth);
                if (mMaxDate != null && current.compareTo(mMaxDate) > 0) {
                    break;
                }
                rows[i] = new CalendarMonthItem(this, current);

            }
            mAdapter.addRows(rows);
            if (scrollToLast) {
                mList.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    private void drawMonthFuture(int cnt) {
        drawMonthFuture(cnt, false);
    }

    private void drawMonthPast(int cnt) {
        drawMonthPast(cnt, false);
    }

    private void drawMonthPast(int cnt, boolean scrollToFirst) {
        mList.post(() -> {
            final CalendarMonthItem[] rows = new CalendarMonthItem[cnt];
            for (int i = 0, k = cnt - 1; i < cnt; i++, k--) {
                DateTime current = mInitial.minusMonths(++mPastMonth);
                if (mMinDate != null && current.compareTo(mMinDate) < 0) {
                    break;
                }
                rows[k] = new CalendarMonthItem(this, current);
            }

            mAdapter.addRowsTop(rows);
            if (scrollToFirst) {
                mList.scrollToPosition(0);
            }
        });
    }

}
