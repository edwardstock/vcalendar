package com.edwardstock.vcalendarapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.edwardstock.vcalendar.OnSelectionListener;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.VCalendar;
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


        calendar.getSelectionDispatcher().addOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded) {
            }
        });


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
}
