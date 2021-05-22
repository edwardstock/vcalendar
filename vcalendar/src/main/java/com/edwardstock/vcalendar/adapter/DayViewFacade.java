package com.edwardstock.vcalendar.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.edwardstock.vcalendar.R;

import java.lang.ref.WeakReference;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * vcalendarapp. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class DayViewFacade {
    private final WeakReference<TextView> mView;

    public DayViewFacade(final TextView view) {
        mView = new WeakReference<>(view);
    }

    /**
     * @param selectedColorState
     * @see ColorStateList
     */
    public void setSelectedState(boolean selectedColorState) {
        getView().setSelected(selectedColorState);
    }

    /**
     * @param color HEX color
     */
    public void setTextColor(@ColorInt int color) {
        getView().setTextColor(color);
    }

    /**
     * @param colorRes Color resource id
     */
    public void setTextColorRes(@ColorRes int colorRes) {
        setTextColor(getView().getContext().getResources().getColor(colorRes));
    }

    public void setTextColor(ColorStateList colors) {
        getView().setTextColor(colors);
    }

    public void setBackground(Drawable drawable) {
        getView().setBackground(drawable);
    }

    public void setBackgroundResource(@DrawableRes int resId) {
        getView().setBackgroundResource(resId);
    }

    public void setBackgroundColor(@ColorInt int color) {
        getView().setBackgroundColor(color);
    }

    public void setClickable(boolean clickable) {
        getView().setClickable(clickable);
    }

    public void reset() {
        getView().setTextColor(getView().getContext().getResources().getColor(R.color.vcal_text_color_dark));
        getView().setBackground(null);
        getView().setSelected(false);
        getView().setClickable(true);
    }

    protected TextView getView() {
        return mView.get();
    }


}
