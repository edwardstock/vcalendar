package com.edwardstock.vcalendarapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.VCalendar;
import com.edwardstock.vcalendar.adapter.DayViewFacade;
import com.edwardstock.vcalendar.decorators.ConnectedDayDecorator;
import com.edwardstock.vcalendar.decorators.DefaultDayDecorator;
import com.edwardstock.vcalendar.models.CalendarDay;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VCalendar calendar = findViewById(R.id.cal);
        calendar.getSelectionDispatcher().setMode(SelectionMode.RANGE);

        calendar.addDayDecorator(new DefaultDayDecorator());
        calendar.addDayDecorator(new CustomDecor(new DateTime("2018-03-27")));


        //        List<DateTime> selections = new ArrayList<>(2);
        //        calendar.setInitialDate("2018-05-10");
        //        selections.add(new DateTime("2018-05-10"));
        //        selections.add(new DateTime("2018-05-20"));
        //        calendar.getSelectionDispatcher().setSelections(selections);
        //
        //        calendar.getSelectionDispatcher().addOnSelectionListener(new OnSelectionListener() {
        //            @Override
        //            public void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded) {
        //                Stream.of(calendarDays).forEach(item -> Timber.d(item.toString()));
        //            }
        //        });
        //
        //        calendar.addOnMonthAddListener(month -> Timber.d("Append month: %s", month.toString()));
        //        calendar.addOnMonthBindListener(month -> Timber.d("Bind month: %s", month.toString()));
        //        calendar.addOnMonthUnbindListener(month -> Timber.d("Unbind month: %s", month.toString()));
        //
        //
        new Handler().postDelayed(() -> {
            List<DateTime> dates = new ArrayList<>(5);

            for (int i = 0; i < 5; i++) {
                dates.add(new DateTime().plusDays(i + 1));
                calendar.getDayOrCreate(new DateTime().plusDays(i + 1)).setSelected(true);
            }
            try {
                Thread.sleep(1000);
                calendar.updateDays(dates);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, 5000);
    }

    public final static class CustomDecor extends ConnectedDayDecorator {

        private DateTime mShouldDate;

        CustomDecor(DateTime dt) {
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
