package com.edwardstock.vcalendarapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.OnSelectionListener;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.VCalendar;
import com.edwardstock.vcalendar.adapter.DayViewFacade;
import com.edwardstock.vcalendar.decorators.ConnectedDayDecorator;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VCalendar calendar = findViewById(R.id.cal);
        calendar.getSelectionDispatcher().setMode(SelectionMode.RANGE);

        calendar.setInitialMonth("2018-03");
        calendar.setMinDate(new DateTime().minusDays(3)).setMinDateCut(true);
        calendar.reset();


        calendar.getSelectionDispatcher().setEnableContinuousSelection(false);

        calendar.getSelectionDispatcher().addOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded) {
                Stream.of(calendarDays).forEach(item -> Timber.d(item.toString()));
            }
        });
        calendar.addOnMonthAddListener(month -> Timber.d("Append month: %s", month.toString()));
        calendar.addOnMonthBindListener(month -> Timber.d("Bind month: %s", month.toString()));
        calendar.addOnMonthUnbindListener(month -> Timber.d("Unbind month: %s", month.toString()));


        new Handler().postDelayed(() -> {
            Timber.d("Update in thread: %s", Thread.currentThread().getName());
            List<DateTime> toUpdate = new ArrayList<>(40);
            DateTime today = new DateTime();
            for (int i = today.getDayOfMonth(); i < 40; i++) {
                toUpdate.add(today.plusDays(i));
            }
            calendar.updateDays(toUpdate);
        }, 3000);


    }

    public final static class CustomDecorator extends ConnectedDayDecorator {

        private DateTime mShouldDate;

        CustomDecorator(DateTime dt) {
            mShouldDate = dt;
        }

        @Override
        public void decorate(CalendarDay day, DayViewFacade tv, int neighbourhood) {
            if (day.isSelected()) {
                super.decorate(day, tv, neighbourhood);
            } else {
                tv.setBackgroundResource(R.drawable.shape_round_yellow);
            }
        }


        @Override
        public int getSelectedBeginBackgroundRes() {
            return R.drawable.bg_custom_calendar_day_selection_begin;
        }

        @Override
        public int getSelectedMiddleBackgroundRes() {
            return R.drawable.bg_custom_calendar_day_selection_middle;
        }

        @Override
        public int getSelectedEndBackgroundRes() {
            return R.drawable.bg_custom_calendar_day_selection_end;
        }

        @Override
        public int getSelectedSingleBackgroundRes() {
            return R.drawable.bg_custom_calendar_day_selection_single;
        }

        @Override
        public boolean shouldDecorate(CalendarDay calendarDay) {
            return calendarDay.getDateTime().getDayOfMonth() == mShouldDate.getDayOfMonth();
        }
    }
}
