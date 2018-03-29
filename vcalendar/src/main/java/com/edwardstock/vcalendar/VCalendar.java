package com.edwardstock.vcalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.adapter.CalendarMonthItem;
import com.edwardstock.vcalendar.adapter.DaysAdapter;
import com.edwardstock.vcalendar.adapter.MultiRowAdapter;
import com.edwardstock.vcalendar.decorators.DayDecorator;
import com.edwardstock.vcalendar.decorators.DefaultDayDecorator;
import com.edwardstock.vcalendar.decorators.DisabledRangeDayDecorator;
import com.edwardstock.vcalendar.handlers.MultipleSelectionHandler;
import com.edwardstock.vcalendar.handlers.RangeSelectionHandler;
import com.edwardstock.vcalendar.handlers.SelectionDispatcher;
import com.edwardstock.vcalendar.handlers.SingleSelectionHandler;
import com.edwardstock.vcalendar.models.CalendarDay;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.YearMonth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@SuppressWarnings("Convert2MethodRef")
public class VCalendar extends FrameLayout implements CalendarHandler {

    private final String[] mMonthsRu = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",};
    private String[] mMonths;
    private String[] mDaysOfWeek;

    @DrawableRes private int mSelectedMiddleBackgroundRes;
    @DrawableRes private int mSelectedEndBackgroundRes;
    @DrawableRes private int mSelectedSingleBackgroundRes;
    @DrawableRes private int mSelectedBeginBackgroundRes;
    private int mOrientation = LinearLayoutManager.VERTICAL;
    private RecyclerView mList;
    private MultiRowAdapter mAdapter;
    private Map<DateTime, CalendarDay> mDayMap = new HashMap<>();
    private int mFutureMonth = 0;
    private int mPastMonth = 0;
    private DateTime mInitial;
    private Map<YearMonth, CalendarMonthItem> mRowMap = new HashMap<>();
    private Set<DayDecorator> mDayDecorators = new HashSet<>();
    private List<OnDayClickListener> mOnDayClickListeners = new ArrayList<>();
    private DateTime mMinDate;
    private DateTime mMaxDate;
    private boolean mEnableLegend;
    private List<OnMonthAddListener> mMonthListeners = new ArrayList<>();
    private List<CalendarMonthItem.OnBindListener> mOnMonthBindListeners = new ArrayList<>();
    private List<CalendarMonthItem.OnUnbindListener> mOnMonthUnbindListeners = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private SelectionDispatcher mSelectionDispatcher;


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
    public VCalendar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(getContext(), R.layout.vcalendar_main, this);
        TypedArray def = getContext().obtainStyledAttributes(attrs, R.styleable.VCalendar, defStyleAttr, defStyleRes);

        mSelectedBeginBackgroundRes = def.getResourceId(R.styleable.VCalendar_selectionBeginBackground, R.drawable.bg_calendar_day_selection_begin);
        mSelectedMiddleBackgroundRes = def.getResourceId(R.styleable.VCalendar_selectionMiddleBackground, R.drawable.bg_calendar_day_selection_middle);
        mSelectedEndBackgroundRes = def.getResourceId(R.styleable.VCalendar_selectionEndBackground, R.drawable.bg_calendar_day_selection_end);
        mSelectedSingleBackgroundRes = def.getResourceId(R.styleable.VCalendar_selectionSingleBackground, R.drawable.bg_calendar_day_selection_single);
        mOrientation = def.getInt(R.styleable.VCalendar_orientation, LinearLayoutManager.VERTICAL);

        mEnableLegend = def.getBoolean(R.styleable.VCalendar_enableLegend, true);

        if (def.getBoolean(R.styleable.VCalendar_enableDefaultDecorator, true)) {
            addDayDecorator(new DefaultDayDecorator());
        }

        if (def.hasValue(R.styleable.VCalendar_initialDate)) {
            setInitialDate(def.getString(R.styleable.VCalendar_initialDate));
        } else {
            setInitialDate(new DateTime());
        }

        if (def.hasValue(R.styleable.VCalendar_monthNamesArray)) {
            int stringsRes = def.getResourceId(R.styleable.VCalendar_monthNamesArray, 0);
            if (stringsRes != 0) {
                setMonthNames(stringsRes);
            }
        }

        if (mMonths == null || mMonths.length == 0) {
            mMonths = new String[12];
            final DateTime mdt = new DateTime().monthOfYear().withMinimumValue();
            for (int i = 1; i <= 12; i++) {
                mMonths[i - 1] = firstUppercase(mdt.withMonthOfYear(i).monthOfYear().getAsText());
            }
        }

        if (def.hasValue(R.styleable.VCalendar_daysOfWeekNamesArray)) {
            int stringsRes = def.getResourceId(R.styleable.VCalendar_daysOfWeekNamesArray, 0);
            if (stringsRes != 0) {
                setDaysOfWeekNames(stringsRes);
            }
        }

        if (mDaysOfWeek == null || mDaysOfWeek.length == 0) {
            mDaysOfWeek = new String[7];
            final DateTime mdt = new DateTime().dayOfWeek().withMinimumValue();
            for (int i = 1; i <= 7; i++) {
                mDaysOfWeek[i - 1] = firstUppercase(mdt.withDayOfWeek(i).dayOfWeek().getAsShortText());
            }
        }

        mSelectionDispatcher = new SelectionDispatcher(new SelectionDispatcher.Delegate() {
            @Override
            public void onUpdate() {
                updateSelections();
            }

            @Override
            public void onClear() {
                updateSelectionsAndClear();
            }

            @Override
            public CalendarDay getDayOrCreate(DateTime dateTime) {
                return VCalendar.this.getDayOrCreate(dateTime);
            }

            @Override
            public CalendarDay getDay(DateTime dateTime) {
                return VCalendar.this.getDay(dateTime);
            }

            @Override
            public void onSetMinLimit(DateTime dateTime) {
                addDayDecorator(new DisabledRangeDayDecorator(getContext(), DisabledRangeDayDecorator.RangeMode.BEFORE, dateTime));
            }

            @Override
            public void onSetMaxLimit(DateTime dateTime) {
                addDayDecorator(new DisabledRangeDayDecorator(getContext(), DisabledRangeDayDecorator.RangeMode.AFTER, dateTime));
            }

            @Override
            public void onSetSelections() {
                if (getSelectionDispatcher().getSelections().size() > 0) {
                    CalendarDay firstSelected = getSelectionDispatcher().getSelection(0);
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
                }
            }
        });

        getSelectionDispatcher().setMode(def.getInt(R.styleable.VCalendar_selectionMode, SelectionMode.NONE));

        getSelectionDispatcher().attachHandler(SelectionMode.RANGE, RangeSelectionHandler.class);
        getSelectionDispatcher().attachHandler(SelectionMode.MULTIPLE, MultipleSelectionHandler.class);
        getSelectionDispatcher().attachHandler(SelectionMode.SINGLE, SingleSelectionHandler.class);
        getSelectionDispatcher().attachHandler(SelectionMode.EVEN, SingleSelectionHandler.class);
        getSelectionDispatcher().attachHandler(SelectionMode.ODD, SingleSelectionHandler.class);

        mLayoutManager = new LinearLayoutManager(getContext(), mOrientation, false);
        mList = findViewById(R.id.mainList);
        mList.setLayoutManager(mLayoutManager);
        mList.setItemViewCacheSize(10);
        mList.setDrawingCacheEnabled(true);
        mList.setAdapter(getAdapter());

        def.recycle();

        initData();
    }

    public VCalendar setSelectedBeginBackgroundRes(@DrawableRes int selectedBeginBackgroundRes) {
        mSelectedBeginBackgroundRes = selectedBeginBackgroundRes;
        return this;
    }

    public VCalendar setSelectedEndBackgroundRes(@DrawableRes int selectedEndBackgroundRes) {
        mSelectedEndBackgroundRes = selectedEndBackgroundRes;
        return this;
    }

    public VCalendar setSelectedSingleBackgroundRes(@DrawableRes int selectedSingleBackgroundRes) {
        mSelectedSingleBackgroundRes = selectedSingleBackgroundRes;
        return this;
    }

    public VCalendar setSelectedMiddleBackgroundRes(@DrawableRes int selectedMiddleBackgroundRes) {
        mSelectedMiddleBackgroundRes = selectedMiddleBackgroundRes;
        return this;
    }

    public VCalendar setOrientation(@RecyclerView.Orientation int orientation) {
        mOrientation = orientation;
        if (mLayoutManager != null) {
            mLayoutManager.setOrientation(orientation);
        }
        return this;
    }

    /**
     * Be carefully, this method reinitializes whole data, if you have "month add" listeners, attach them before setting initial date
     * or just set initial date in your layout
     *
     * @param year
     * @param month month is 1-based, january is 1
     */
    public VCalendar setInitialDate(int year, @IntRange(from = 1, to = 12) int month) {
        return setInitialDate(new DateTime().withYear(year).withMonthOfYear(clamp(month, 1, 12)));
    }

    public VCalendar setInitialDate(YearMonth yearMonth) {
        return setInitialDate(new DateTime().withMonthOfYear(yearMonth.getMonthOfYear()).withYear(yearMonth.getYear()));
    }

    public VCalendar setInitialDate(String yearMonth) {
        return setInitialDate(YearMonth.parse(yearMonth));
    }

    public VCalendar setInitialDate(DateTime dt) {
        mInitial = dt.withDayOfMonth(1).withTime(0, 0, 0, 0);
        reset();
        return this;
    }

    public VCalendar setInitialDate(Date date) {
        return setInitialDate(new DateTime(date));
    }

    public VCalendar addDayDecorator(DayDecorator decorator) {
        mDayDecorators.add(decorator);
        return this;
    }

    public VCalendar removeDayDecorator(DayDecorator decorator) {
        mDayDecorators.remove(decorator);
        return this;
    }

    public VCalendar clearDayDecorators() {
        mDayDecorators.clear();
        return this;
    }

    @Override
    public CalendarDay getDay(DateTime dateTime) {
        return mDayMap.get(dateTime.withTime(0, 0, 0, 0));
    }

    public int getDaysRenderedCount() {
        return mDayMap.size();
    }

    public int getMonthsRenderedCount() {
        return mRowMap.size();
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

    @NonNull
    @Override
    public CalendarDay getPreviousDay(CalendarDay current) {
        return getDayOrCreate(current.getDateTime().minusDays(1));
    }

    @DrawableRes
    @Override
    public int getSelectedMiddleBackgroundRes() {
        return mSelectedMiddleBackgroundRes;
    }

    @DrawableRes
    @Override
    public int getSelectedEndBackgroundRes() {
        return mSelectedEndBackgroundRes;
    }

    @DrawableRes
    @Override
    public int getSelectedBeginBackgroundRes() {
        return mSelectedBeginBackgroundRes;
    }

    @DrawableRes
    @Override
    public int getSelectedSingleBackgroundRes() {
        return mSelectedSingleBackgroundRes;
    }

    public VCalendar addOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListeners.add(listener);
        return this;
    }

    public VCalendar removeOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListeners.remove(listener);
        return this;
    }

    public VCalendar clearOnDayClickListeners() {
        mOnDayClickListeners.clear();
        return this;
    }

    static String firstUppercase(String input) {
        if (input == null) {
            return null;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    public boolean isEnabledLegend() {
        return mEnableLegend;
    }

    @Override
    public Set<DayDecorator> getDayDecorators() {
        return mDayDecorators;
    }

    public VCalendar setMonthNames(@ArrayRes int stringArrNames) {
        setMonthNames(getContext().getResources().getStringArray(stringArrNames));
        return this;
    }

    public VCalendar setDaysOfWeekNames(@ArrayRes int stringArrNames) {
        return setDaysOfWeekNames(getContext().getResources().getStringArray(stringArrNames));
    }

    public VCalendar setDaysOfWeekNames(String[] names) {
        if (names == null || names.length != 7 || Stream.of(names).filter(item -> item == null).count() > 0) {
            throw new IllegalArgumentException("Week has exactly 7 days, and names can't be null or empty");
        }
        mDaysOfWeek = names;
        return this;
    }

    public VCalendar setMonthNames(String[] monthsNames) {
        if (monthsNames == null || monthsNames.length != 12 || Stream.of(monthsNames).filter(item -> item == null).count() > 0) {
            throw new IllegalArgumentException("Year has exactly 12 months, and names can't be null or empty");
        }
        mMonths = monthsNames;
        return this;
    }

    @Override
    public String[] getMonthNames() {
        if (Locale.getDefault().equals(new Locale("ru", "RU"))) {
            return mMonthsRu;
        }

        return mMonths;
    }

    @NonNull
    @Override
    public CalendarDay getNextDay(CalendarDay current) {
        return getDayOrCreate(current.getDateTime().plusDays(1));
    }

    public VCalendar setMinDate(Date date) {
        return setMinDate(new DateTime(date));
    }

    public VCalendar setMinDate(String date) {
        return setMinDate(new DateTime());
    }

    public VCalendar setMinDate(DateTime minDate) {
        mMinDate = minDate;
        return this;
    }

    public VCalendar setMaxDate(String date) {
        return setMaxDate(new DateTime(date));
    }

    public VCalendar setMaxDate(Date date) {
        return setMaxDate(new DateTime(date));
    }

    public VCalendar setMaxDate(DateTime maxDate) {
        mMaxDate = maxDate;
        return this;
    }

    public VCalendar addOnMonthAddListener(OnMonthAddListener listener) {
        mMonthListeners.add(listener);
        return this;
    }

    public VCalendar removeOnMonthAddListener(OnMonthAddListener listener) {
        mMonthListeners.remove(listener);
        return this;
    }

    public VCalendar clearOnMonthAddListeners() {
        mMonthListeners.clear();
        return this;
    }

    public VCalendar addOnMonthBindListener(CalendarMonthItem.OnBindListener listener) {
        mOnMonthBindListeners.add(listener);
        return this;
    }

    public VCalendar addOnMonthUnbindListener(CalendarMonthItem.OnUnbindListener listener) {
        mOnMonthUnbindListeners.add(listener);
        return this;
    }

    public VCalendar removeOnMonthBindListener(CalendarMonthItem.OnBindListener listener) {
        mOnMonthBindListeners.remove(listener);
        return this;
    }

    public VCalendar removeOnMonthUnbindListener(CalendarMonthItem.OnUnbindListener listener) {
        mOnMonthUnbindListeners.remove(listener);
        return this;
    }

    public VCalendar clearOnMonthBindListeners() {
        mOnMonthBindListeners.clear();
        return this;
    }

    public VCalendar clearOnMonthUnbindListener() {
        mOnMonthUnbindListeners.clear();
        return this;
    }

    public final void reset() {
        if (mAdapter == null || mList == null) {
            return;
        }

        mAdapter.clear();
        mList.clearOnScrollListeners();
        mDayMap.clear();
        mRowMap.clear();
        initData();
    }

    @Override
    public SelectionDispatcher getSelectionDispatcher() {
        return mSelectionDispatcher;
    }

    public void updateDaysDate(Collection<Date> dateTimes) {
        updateDays(Stream.of(dateTimes).map(DateTime::new).toList());
    }

    public void updateDaysString(Collection<String> dateTimes) {
        updateDays(Stream.of(dateTimes).map(DateTime::new).toList());
    }

    @Override
    public String[] getDaysOfWeek() {
        return mDaysOfWeek;
    }

    public void updateDay(Date date) {
        updateDay(new DateTime(date));
    }

    public void updateDay(CalendarDay calendarDay) {
        updateDay(calendarDay.getDateTime());
    }

    public void updateDays(Collection<DateTime> dateTimes) {
        final Map<YearMonth, List<DateTime>> daysMap = new HashMap<>();
        for (DateTime day : dateTimes) {
            YearMonth month = new YearMonth(day);
            if (!daysMap.containsKey(month)) {
                daysMap.put(month, new ArrayList<>());
            }
            daysMap.get(month).add(day);
        }

        for (Map.Entry<YearMonth, List<DateTime>> entry : daysMap.entrySet()) {
            if (mRowMap.containsKey(entry.getKey())) {
                for (DateTime dt : entry.getValue()) {
                    mRowMap.get(entry.getKey()).getAdapter().update(dt);
                }
            }
        }

        final List<DateTime> selected = Stream.of(dateTimes)
                .map(item -> getDayOrCreate(item))
                .filter(item -> item.isSelected())
                .map(CalendarDay::getDateTime)
                .toList();
        if (selected != null) {
            getSelectionDispatcher().setSelections(selected);
        }
    }

    public void updateMonthsDate(Collection<Date> months) {
        updateMonths(Stream.of(months).map(YearMonth::new).toList());
    }

    public void updateMonthsDateTime(Collection<DateTime> months) {
        updateMonths(Stream.of(months).map(YearMonth::new).toList());
    }

    public void updateMonthsString(Collection<String> months) {
        updateMonths(Stream.of(months).map(YearMonth::new).toList());
    }

    public void updateMonths(Collection<YearMonth> months) {
        Stream.of(months).forEach(this::updateMonth);
    }

    public void updateMonth(String month) {
        updateMonth(new YearMonth(month));
    }

    public void updateMonth(DateTime month) {
        updateMonth(new YearMonth(month));
    }

    public void updateMonth(Date date) {
        updateMonth(new YearMonth(date));
    }

    public void updateMonth(YearMonth month) {
        if (!mRowMap.containsKey(month)) {
            return;
        }

        mRowMap.get(month).getAdapter().notifyDataSetChanged();
    }

    private int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        }

        return val;
    }

    private void setMonthRow(CalendarMonthItem calendarMonthItem) {
        mRowMap.put(calendarMonthItem.getMonth(), calendarMonthItem);
    }

    private void onDayClick(CalendarDay calendarDay, View dayView, DaysAdapter adapter) {
        getSelectionDispatcher().onClick(dayView, calendarDay);
        Stream.of(mOnDayClickListeners).filter(item -> item != null).forEach(item -> item.onClick(dayView, calendarDay));
    }

    public void updateDay(DateTime dateTime) {
        YearMonth ym = new YearMonth(dateTime);
        if (mRowMap.containsKey(ym)) {
            CalendarMonthItem item = mRowMap.get(ym);
            item.getAdapter().update(dateTime);
        }

        if (getDayOrCreate(dateTime).isSelected()) {
            getSelectionDispatcher().setSelection(getDay(dateTime));
        }
    }

    private void initListPreDraw() {
        if (mList.getChildCount() < 1) {
            Timber.i("No one month added to view");
            return;
        }
        int firstH = mList.getChildAt(0).getHeight();
        int toDrawCount = (int) Math.ceil((float) getHeight() / (float) firstH) + 1;
        drawMonthFuture(toDrawCount);
        drawMonthPast(2);
    }

    private MultiRowAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MultiRowAdapter();
        }
        return mAdapter;
    }

    private void initData() {
        final CalendarMonthItem initialMonth = new CalendarMonthItem(this, mInitial, this::onDayClick).setLifecycle(this::callOnMonthBindListeners, this::callOnMonthUnbindListeners);

        getAdapter().setEnableSorting(true);
        getAdapter().addRow(initialMonth);

        callOnMonthAddListeners(initialMonth);

        if (mList.getHeight() > 0 && mList.getChildCount() > 0) {
            initListPreDraw();
        } else {
            mList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    initListPreDraw();
                    mList.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
        }

        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
                int lastVisible = mLayoutManager.findLastVisibleItemPosition();

                if (lastVisible + 2 >= getAdapter().getItemCount()) {
                    drawMonthFuture(3);
                } else if (firstVisible <= 2) {
                    drawMonthPast(2);
                }
            }
        });
    }

    private void updateSelectionsAndClear() {
        final Map<YearMonth, List<CalendarDay>> daysMap = new HashMap<>();
        for (CalendarDay day : getSelectionDispatcher().getSelections()) {
            YearMonth month = new YearMonth(day.getDateTime());
            day.setSelected(false);
            if (!daysMap.containsKey(month)) {
                daysMap.put(month, new ArrayList<>());
            }
            daysMap.get(month).add(day);
        }

        for (Map.Entry<YearMonth, List<CalendarDay>> entry : daysMap.entrySet()) {
            if (mRowMap.containsKey(entry.getKey())) {
                for (CalendarDay cd : entry.getValue()) {
                    mRowMap.get(entry.getKey()).getAdapter().update(cd);
                }
            }
        }

        getSelectionDispatcher().getSelections().clear();
    }

    private void updateSelections() {
        final Map<YearMonth, List<CalendarDay>> daysMap = new HashMap<>();
        for (CalendarDay day : getSelectionDispatcher().getSelections()) {
            YearMonth month = new YearMonth(day.getDateTime());
            if (!daysMap.containsKey(month)) {
                daysMap.put(month, new ArrayList<>());
            }
            daysMap.get(month).add(day);
        }

        for (Map.Entry<YearMonth, List<CalendarDay>> entry : daysMap.entrySet()) {
            if (mRowMap.containsKey(entry.getKey())) {
                for (CalendarDay cd : entry.getValue()) {
                    mRowMap.get(entry.getKey()).getAdapter().update(cd);
                }
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
                rows[i] = new CalendarMonthItem(this, current, this::onDayClick).setLifecycle(this::callOnMonthBindListeners, this::callOnMonthUnbindListeners);
                callOnMonthAddListeners(rows[i]);
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
                rows[k] = new CalendarMonthItem(this, current, this::onDayClick).setLifecycle(this::callOnMonthBindListeners, this::callOnMonthUnbindListeners);
                callOnMonthAddListeners(rows[k]);
            }

            mAdapter.addRowsTop(rows);
            if (scrollToFirst) {
                mList.scrollToPosition(0);
            }
        });
    }

    private void callOnMonthBindListeners(YearMonth yearMonth) {
        Stream.of(mOnMonthBindListeners).filter(item -> item != null).forEach(item -> item.onBindMonth(yearMonth));
    }

    private void callOnMonthUnbindListeners(YearMonth yearMonth) {
        Stream.of(mOnMonthUnbindListeners).filter(item -> item != null).forEach(item -> item.onUnbindMonth(yearMonth));
    }

    private void callOnMonthAddListeners(CalendarMonthItem monthItem) {
        setMonthRow(monthItem);
        Stream.of(mMonthListeners).filter(item -> item != null).forEach(item -> item.onMonth(monthItem.getMonth()));
    }

}
