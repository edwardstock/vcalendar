package com.edwardstock.vcalendar.adapter;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * vcalendar. 2018
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class NonScrollableGridLayoutManager extends GridLayoutManager {
	public NonScrollableGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public NonScrollableGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}
	public NonScrollableGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	@Override
	public boolean canScrollHorizontally() {
		return false;
	}

	@Override
	public boolean canScrollVertically() {
		return false;
	}
}
