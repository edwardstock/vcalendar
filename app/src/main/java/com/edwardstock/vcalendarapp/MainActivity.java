package com.edwardstock.vcalendarapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Stream;
import com.edwardstock.vcalendar.OnSelectionListener;
import com.edwardstock.vcalendar.SelectionMode;
import com.edwardstock.vcalendar.VCalendar;
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

        List<DateTime> selections = new ArrayList<>(2);
        calendar.setInitialDate("2018-05-10");
        selections.add(new DateTime("2018-05-10"));
        selections.add(new DateTime("2018-05-20"));
        calendar.getSelectionDispatcher().setSelections(selections);

        calendar.getSelectionDispatcher().addOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded) {
                Stream.of(calendarDays).forEach(item -> Timber.d(item.toString()));
            }
        });

        calendar.addOnMonthAddListener(month -> Timber.d("Append month: %s", month.toString()));
        calendar.addOnMonthBindListener(month -> Timber.d("Bind month: %s", month.toString()));
        calendar.addOnMonthUnbindListener(month -> Timber.d("Unbind month: %s", month.toString()));


    }
}
