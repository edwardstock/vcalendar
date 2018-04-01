package com.edwardstock.vcalendar.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Mds. 2017
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */

public interface CalendarAdapterContract {
    interface Row<T extends RecyclerView.ViewHolder> {
        int POSITION_FIRST = -1;
        int POSITION_LAST = 999;
        int POSITION_ORDERED = 0;

        /**
         * views ID
         *
         * @return int
         * @see CalendarAdapter#makeHoldersCache()
         */
        @LayoutRes
        int getItemView();

        /**
         * views position index
         *
         * @return int
         * @see CalendarAdapter
         * @see CalendarAdapterContract.Row
         */
        int getRowPosition();

        /**
         * If view should be visible
         *
         * @return bool
         */
        boolean isVisible();

        /**
         * calling when adapter binds view
         * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
         *
         * @param viewHolder Row view holder
         * @see CalendarAdapter#makeHoldersCache()
         */
        void onBindViewHolder(@NonNull T viewHolder);

        /**
         *
         * @see RecyclerView#onDetachedFromWindow()
         * @param viewHolder
         */
        void onUnbindViewHolder(@NonNull T viewHolder);

        /**
         * View holder class name
         *
         * @return Class
         * @see CalendarAdapter.RowViewHolder
         */
        @NonNull
        Class<T> getViewHolderClass();
    }
}
