package com.craigcleveland.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by craig on 10/7/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private final String TAG = TrailerAdapter.class.getSimpleName();

    private final TrailerAdapterClickHandler mClickHandler;

    private final Context mContext;
    private Cursor mCursor;

    public interface TrailerAdapterClickHandler {
        void onClick(int trailerID);
    }

    public TrailerAdapter(@NonNull Context context, TrailerAdapterClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView mTrailerTextView;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            mTrailerTextView = view.findViewById(R.id.tv_trailer_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getInt(MovieDetailActivity.INDEX_TRAILER_ID));
        }
    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutForListItem = R.layout.trailer_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutForListItem, viewGroup, false);

        view.setFocusable(true);

        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder trailerAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        String trailerName = mCursor.getString(MovieDetailActivity.INDEX_TRAILER_NAME);
        trailerAdapterViewHolder.mTrailerTextView.setText(trailerName);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

}
