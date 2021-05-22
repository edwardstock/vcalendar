package com.edwardstock.vcalendar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.edwardstock.vcalendar.common.Preconditions.checkNotNull;

/**
 * MultiRow adapter
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<CalendarAdapterContract.Row> mItems = new ArrayList<>();
    private SimpleArrayMap<Integer, Class<? extends RecyclerView.ViewHolder>> holderViewIdClassCache = new SimpleArrayMap<>();
    private LayoutInflater layoutInflater;
    private boolean mEnableSorting = true;

    public void addRowsTop(CalendarAdapterContract.Row[] rows) {
        addRowsTop(Stream.of(rows).toList());
    }

    @SuppressWarnings("Convert2MethodRef")
    public void addRowsTop(Collection<CalendarAdapterContract.Row> rows) {
        if (rows.isEmpty()) return;

        final List<CalendarAdapterContract.Row> target = Stream.of(rows)
                .filter(item -> item != null)
                .filter(CalendarAdapterContract.Row::isVisible)
                .toList();

        mItems.addAll(0, target);
        if (mEnableSorting) {
            sort();
        }

        makeHoldersCache();
        notifyItemRangeInserted(0, target.size());
    }

    @SuppressWarnings("Convert2MethodRef")
    public void addRows(CalendarAdapterContract.Row[] rows) {
        if (rows.length == 0) return;

        final List<CalendarAdapterContract.Row> target = Stream.of(rows)
                .filter(item -> item != null)
                .filter(CalendarAdapterContract.Row::isVisible)
                .toList();

        int beforeSize = mItems.size();
        mItems.addAll(target);
        if (mEnableSorting) {
            sort();
        }

        makeHoldersCache();
        notifyItemRangeInserted(beforeSize, target.size());
    }

    public void addRow(CalendarAdapterContract.Row row) {
        if (row == null || !row.isVisible()) {
            return;
        }
        mItems.add(row);
        if (mEnableSorting) {
            sort();
        }

        makeHoldersCache();
        notifyItemInserted(mItems.size());
    }

    public void setEnableSorting(boolean enableSorting) {
        mEnableSorting = enableSorting;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        if (viewType == View.NO_ID || viewType == 0) {
            throw new RuntimeException("Layout id can't be 0");
        }

        View v = layoutInflater.inflate(viewType, parent, false);

        RecyclerView.ViewHolder viewHolder = null;
        Throwable cause = null;
        try {
            viewHolder = findViewHolder(viewType, v);
        } catch (NoSuchMethodException e) {
            cause = e;
            Timber.e(e, "Error finding ViewHolder");
        } catch (IllegalAccessException e) {
            cause = e;
            Timber.e(e, "Error finding ViewHolder");
        } catch (InvocationTargetException e) {
            cause = e;
            Timber.e(e, "Error finding ViewHolder");
        } catch (InstantiationException e) {
            cause = e;
            Timber.e(e, "Error finding ViewHolder");
        }

        if (viewHolder == null) {
            throw new RuntimeException(cause);
        }

        return viewHolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CalendarAdapterContract.Row item = getItemByPosition(position);
        item.onBindViewHolder(holder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getAdapterPosition();
        if (position < 0) {
            return;
        }

        CalendarAdapterContract.Row item = getItemByPosition(position);
        item.onUnbindViewHolder(holder);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemByPosition(position).getItemView();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void clear() {
        if (mItems.isEmpty()) return;
        mItems.clear();
        notifyDataSetChanged();
        holderViewIdClassCache.clear();
    }

    public CalendarAdapterContract.Row getItemByPosition(int position) {
        return mItems.get(position);
    }

    public void sort() {
        Collections.sort(mItems, new RowComparator());
    }


    /**
     * ViewHolder's class cached indexer
     */
    @SuppressWarnings("unchecked")
    protected void makeHoldersCache() {
        checkNotNull(mItems, "Wow! Rows can't be null");
        holderViewIdClassCache.clear();
        holderViewIdClassCache = new SimpleArrayMap<>(mItems.size());

        for (CalendarAdapterContract.Row item : mItems) {
            checkNotNull(item);
            if (item instanceof SortableRow) {
                checkNotNull(item.getViewHolderClass(),
                             "Row " + (((SortableRow) item).getRow().getClass()) + " does not have valid ViewHolder class");
            } else {
                checkNotNull(item.getViewHolderClass(),
                             "Row " + item.getClass() + " does not have valid ViewHolder class");
            }

            holderViewIdClassCache.put(item.getItemView(), item.getViewHolderClass());
        }
    }

    private boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    @NonNull
    private RecyclerView.ViewHolder findViewHolder(@LayoutRes int viewId, View view)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends RecyclerView.ViewHolder> holderClass = holderViewIdClassCache.get(viewId);
        if (holderClass == null) {
            makeHoldersCache();
        }
        holderClass = holderViewIdClassCache.get(viewId);
        if (holderClass == null) {
            throw new RuntimeException(
                    "Can't findStream ViewHolder for view " + viewId);
        }
        if (isInnerClass(holderClass)) {
            throw new RuntimeException("Class should be static!");
        }
        return holderClass.getDeclaredConstructor(View.class).newInstance(view);
    }

    public static class RowComparator implements Comparator<CalendarAdapterContract.Row> {
        @Override
        public int compare(CalendarAdapterContract.Row o1, CalendarAdapterContract.Row o2) {
            return o1.getRowPosition() - o2.getRowPosition();
        }
    }
}
