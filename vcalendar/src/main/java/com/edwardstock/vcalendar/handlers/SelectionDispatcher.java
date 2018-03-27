package com.edwardstock.vcalendar.handlers;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.OnDayClickListener;
import com.edwardstock.vcalendar.OnSelectionListener;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edwardstock.vcalendar.common.Preconditions.checkNotNull;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class SelectionDispatcher implements OnDayClickListener {
    //    private final Consumer<List<CalendarDay>> mSelections;
    private final Delegate mDelegate;
    short selectionClickCount = 0;
    private List<CalendarDay> mSelections = new ArrayList<>();
    private DateTime mDisabledBeforeDate;
    private DateTime mDisabledAfterDate;
    private int mLimit = 0;
    private Map<Integer, BaseHandler> mHandlers = new HashMap<>();
    private List<OnSelectionListener> mOnSelectionListeners = new ArrayList<>();
    private int mMode;


    public SelectionDispatcher(Delegate delegate) {
        mDelegate = delegate;
    }

    public final void addOnSelectionListener(OnSelectionListener listener) {
        mOnSelectionListeners.add(listener);
    }

    public final void removeOnSelectionListener(OnSelectionListener listener) {
        mOnSelectionListeners.remove(listener);
    }

    public final void clearOnSelectionListeners() {
        mOnSelectionListeners.clear();
    }

    public final void setDisabledBeforeDate(Date date) {
        setDisabledBeforeDate(new DateTime(date));
    }

    public final void setDisabledBeforeDate(String date) {
        setDisabledBeforeDate(new DateTime(date));

    }

    public final void setDisabledBeforeDate(DateTime dateTime) {
        mDisabledBeforeDate = dateTime.minusDays(1);
        mDelegate.onSetMinLimit(dateTime);
    }

    public final void setDisabledAfterDate(Date date) {
        setDisabledAfterDate(new DateTime(date));
    }

    public final void setDisabledAfter(String date) {
        setDisabledAfterDate(new DateTime(date));
    }

    public final void setDisabledAfterDate(DateTime dateTime) {
        mDisabledAfterDate = dateTime.plusDays(1);
        mDelegate.onSetMaxLimit(dateTime);
    }

    @Override
    public void onClick(View dayView, CalendarDay calendarDay) {
        if (checkDayCantSelect(calendarDay)) {
            return;
        }

        BaseHandler handler = mHandlers.get(getMode());
        if (handler != null) {
            handler.onClick(dayView, calendarDay);
        }
    }

    /**
     * /**
     *
     * @param mode         you own mode, just integer constant
     * @param handlerClass Selection handler class
     * @return chain
     * @throws RuntimeException if can't instantiate handler
     * @see SelectionMode via this annotation constants, you can override default behavior
     * @see RangeSelectionHandler
     * @see MultipleSelectionHandler
     * @see SingleSelectionHandler
     * ...
     */
    public SelectionDispatcher attachHandler(int mode, Class<? extends BaseHandler> handlerClass) {
        Throwable t = null;
        try {
            mHandlers.put(mode, handlerClass.getDeclaredConstructor(SelectionDispatcher.class).newInstance(this));
        } catch (InstantiationException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (InvocationTargetException e) {
            t = e;
        } catch (NoSuchMethodException e) {
            t = e;
        }

        if (t != null) {
            throw new RuntimeException(t);
        }
        return this;
    }

    /**
     * @param mode
     * @return
     * @see SelectionMode
     * @see SelectionMode#NONE
     * @see SelectionMode#SINGLE
     * @see SelectionMode#MULTIPLE
     * @see SelectionMode#RANGE
     * @see SelectionMode#EVEN
     * @see SelectionMode#ODD
     * <p>
     * or custom if has set via
     * @see SelectionDispatcher#attachHandler(int, Class)
     */
    public void setMode(int mode) {
        mMode = mode;
    }

    public int getMode() {
        return mMode;
    }

    /**
     * @param limit ≤0 - unlimited
     */
    public void setLimit(@IntRange(from = 0) int limit) {
        mLimit = limit;
    }

    public void clearSelections() {
        mDelegate.onClear();
        mSelections.clear();
    }

    public CalendarDay getDayOrCreate(DateTime dateTime) {
        return mDelegate.getDayOrCreate(dateTime);
    }

    public CalendarDay getDay(DateTime dateTime) {
        return mDelegate.getDay(dateTime);
    }

    public final void setSelection(CalendarDay selections) {
        setSelections(new DateTime[]{selections.getDateTime()});
    }

    public final void setSelections(DateTime[] selections) {
        setSelections(Stream.of(checkNotNull(selections, "Selections can't be null")).toList());
    }

    public final void setSelections(@NonNull Date[] selections) {
        setSelections(Stream.of(checkNotNull(selections, "Selections can't be null")).map(DateTime::new).toList());
    }

    public final void setSelections(@NonNull String[] datesStrings) {
        setSelections(Stream.of(checkNotNull(datesStrings, "Selections can't be null")).map(DateTime::new).toList());
    }

    public void setSelections(List<DateTime> selections) {
        BaseHandler handler = mHandlers.get(getMode());
        if (handler == null) {
            return;
        }

        if (selections == null || selections.size() == 0 || getMode() == SelectionMode.NONE) {
            return;
        }

        final List<DateTime> valid = Stream.of(selections).filter(item -> item != null).toList();
        if (valid.size() == 2 && valid.get(0).equals(valid.get(1))) {
            selections.remove(1);
        }

        // make next click as new selection
        selectionClickCount = 2;
        handler.setSelections(Stream.of(valid).map(this::getDayOrCreate).toList());

        updateSelections();
    }

    public int getLimit() {
        return mLimit;
    }

    public boolean hasSelectionLimit() {
        return mLimit > 0;
    }

    public final void updateSelections() {
        if (mDelegate != null) {
            mDelegate.onUpdate();
        }
    }

    public List<CalendarDay> getSelections() {
        return mSelections;
    }

    public CalendarDay getSelection(int idx) {
        return mSelections.get(idx);
    }

    public CalendarDay getSelection(int idx, CalendarDay defValue) {
        CalendarDay out = getSelection(idx);
        if (out == null) {
            return defValue;
        }

        return out;
    }

    public List<CalendarDay> getSelectionsImmutable() {
        return new ArrayList<>(mSelections);
    }

    final void callOnSelectionListeners(boolean isLimitExceeded) {
        for (OnSelectionListener l : mOnSelectionListeners) {
            l.onSelected(getSelections(), isLimitExceeded);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    protected boolean checkDayCantSelect(CalendarDay calendarDay) {
        if (mDisabledAfterDate == null && mDisabledBeforeDate == null) {
            return false;
        }

        if (mDisabledBeforeDate != null && calendarDay.getDateTime().compareTo(mDisabledBeforeDate) < 0) {
            return true;
        }

        if (mDisabledAfterDate != null && calendarDay.getDateTime().compareTo(mDisabledAfterDate) > 0) {
            return true;
        }

        return false;
    }

    public interface Delegate {
        void onUpdate();
        void onClear();
        CalendarDay getDayOrCreate(DateTime dateTime);
        CalendarDay getDay(DateTime dateTime);
        void onSetMinLimit(DateTime dateTime);
        void onSetMaxLimit(DateTime dateTime);
        void onSetSelections();

    }
}
