package com.edwardstock.vcalendar.adapter;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * vcalendarapp. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@IntDef({
        Neighbourhood.NO_NEIGHBOURS,
        Neighbourhood.IS_BEGIN,
        Neighbourhood.IS_MIDDLE,
        Neighbourhood.IS_END
})
@Retention(RetentionPolicy.SOURCE)
public @interface Neighbourhood {
    int NO_NEIGHBOURS = 0;
    int IS_BEGIN = 1;
    int IS_MIDDLE = 2;
    int IS_END = 3;
}
