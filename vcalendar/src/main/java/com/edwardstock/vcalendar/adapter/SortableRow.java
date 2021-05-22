package com.edwardstock.vcalendar.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Stars. 2017
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class SortableRow<V extends RecyclerView.ViewHolder, T extends CalendarAdapterContract.Row<V>> implements
        CalendarAdapterContract.Row<V> {

    private final T row;
    private final int position;

    public SortableRow(final T row, int position) {
        this.row = row;
        this.position = position;
    }

    public T getRow() {
        return row;
    }

    @Override
    public int getItemView() {
        return row.getItemView();
    }

    @Override
    public int getRowPosition() {
        return position;
    }

    @Override
    public boolean isVisible() {
        return row.isVisible();
    }

    @Override
    public void onBindViewHolder(@NonNull V viewHolder) {
        row.onBindViewHolder(viewHolder);
    }

    @Override
    public void onUnbindViewHolder(@NonNull V viewHolder) {
        row.onUnbindViewHolder(viewHolder);
    }

    @NonNull
    @Override
    public Class<V> getViewHolderClass() {
        return row.getViewHolderClass();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CalendarAdapterContract.Row) {
            return ((CalendarAdapterContract.Row) obj).getRowPosition() == getRowPosition()
                    && ((CalendarAdapterContract.Row) obj).getItemView() == getItemView()
                    && ((CalendarAdapterContract.Row) obj).isVisible() == isVisible()
                    && ((CalendarAdapterContract.Row) obj).getViewHolderClass() == getViewHolderClass();
        }

        return row.equals(obj);
    }
}


