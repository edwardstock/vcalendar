package com.edwardstock.vcalendar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * vcalendar. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@IntDef({SelectionMode.NONE, SelectionMode.SINGLE, SelectionMode.MULTIPLE, SelectionMode.RANGE, SelectionMode.EVEN, SelectionMode.ODD,
})
@Retention(RetentionPolicy.SOURCE)
public @interface SelectionMode {
    int NONE = 0;
    int SINGLE = 1;
    int MULTIPLE = 2;
    int RANGE = 3;
    int EVEN = 4;
    int ODD = 5;
}
