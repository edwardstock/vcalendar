package ru.esdev.vcalendar;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@IntDef({
        VCalendar.SELECTION_NONE,
        VCalendar.SELECTION_SINGLE,
        VCalendar.SELECTION_MULTIPLE,
        VCalendar.SELECTION_RANGE
})
@Retention(RetentionPolicy.SOURCE)
public @interface SelectionMode {
}
