package ru.esdev.vcalendarapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.annimon.stream.Stream;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ru.esdev.vcalendar.OnSelectionListener;
import ru.esdev.vcalendarapp.R;
import ru.esdev.vcalendar.VCalendar;
import ru.esdev.vcalendar.models.CalendarDay;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		VCalendar calendar = findViewById(R.id.cal);

		List<DateTime> selections = new ArrayList<>(2);
		DateTime init = new DateTime().minusMonths(5).minusDays(15);
		selections.add(init);
		selections.add(init.plusDays(5));

		calendar.setSelections(selections);

		calendar.setMaxDate(new DateTime("2018-12-31"));
		calendar.setSelectionDisabledBefore(init);
		calendar.setSelectionDisabledAfter(init.plusDays(5));

        calendar.addOnSelectionListener(new OnSelectionListener() {
            @Override
            public void onSelected(List<CalendarDay> calendarDays, boolean limitExceeded) {
                Stream.of(calendarDays)
                        .forEach(item-> Timber.d(item.toString()));
            }
        });


        Timber.d("First.compareTo(last): %d", selections.get(0).compareTo(selections.get(1)));
        Timber.d("Last.compareTo(first): %d", selections.get(1).compareTo(selections.get(0)));


	}
}
