package com.craigcleveland.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by craig on 11/18/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private static final String TAG = "CCDEBUG-" + ReviewAdapter.class.getSimpleName();

    private final ReviewAdapaterClickHandler mClickHandler;

    private final Context mContext;
    private Cursor mCursor;

    public interface ReviewAdapaterClickHandler {
        void onClick(String reviewAuthor, String reviewContent);
    }

    public ReviewAdapter(@NonNull Context context, ReviewAdapaterClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView mReviewTextView;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mReviewTextView = view.findViewById(R.id.tv_review_author);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapaterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapaterPosition);
            mClickHandler.onClick(mCursor.getString(MovieDetailActivity.INDEX_REVIEW_AUTHOR),
                    mCursor.getString(MovieDetailActivity.INDEX_REVIEW_CONTENT));
        }

    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutForListItem = R.layout.review_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutForListItem, viewGroup, false);

        view.setFocusable(true);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder reviewAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        String reviewAuthor = mCursor.getString(MovieDetailActivity.INDEX_REVIEW_AUTHOR);
        reviewAdapterViewHolder.mReviewTextView.setText(reviewAuthor);
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
