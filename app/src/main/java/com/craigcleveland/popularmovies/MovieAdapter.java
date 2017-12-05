package com.craigcleveland.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private final String TAG = MovieAdapter.class.getSimpleName();

    private final MovieAdapterClickHandler mClickHandler;

    private final Context mContext;
    private Cursor mCursor;

    public interface MovieAdapterClickHandler {
        void onClick(int movieID, int position);
    }

    public MovieAdapter(@NonNull Context context, MovieAdapterClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }


    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mPosterImageView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mPosterImageView = view.findViewById(R.id.iv_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getInt(MainActivity.INDEX_MOVIE_ID),
                    adapterPosition);
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutForListItem, viewGroup, false);

        view.setFocusable(true);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        String movieTitle = mCursor.getString(MainActivity.INDEX_MOVIE_TITLE);
        movieAdapterViewHolder.mPosterImageView.setContentDescription(movieTitle);

        String posterURL =
                NetworkUtils.buildPosterURL(mCursor.getString(MainActivity.INDEX_MOVIE_POSTER));
        //Context context = movieAdapterViewHolder.mPosterImageView.getContext();
        Picasso.with(mContext).load(posterURL).into(movieAdapterViewHolder.mPosterImageView);

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
