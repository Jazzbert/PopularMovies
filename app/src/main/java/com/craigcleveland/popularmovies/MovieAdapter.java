package com.craigcleveland.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private String[][] mMovieData;

    private final MovieAdapterClickHandler mClickHandler;

    public interface MovieAdapterClickHandler {
        void onClick(String movieDetails[]);
    }

    public MovieAdapter(MovieAdapterClickHandler clickHandler) {
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
            mClickHandler.onClick(mMovieData[adapterPosition]);
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutForListItem, viewGroup, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        String movieTitle = mMovieData[position][MovieDBJsonUtils.TITLE_ITEM];
        movieAdapterViewHolder.mPosterImageView.setContentDescription(movieTitle);

        String posterURL =
                NetworkUtils.buildPosterURL(mMovieData[position][MovieDBJsonUtils.POSTER_ITEM]);
        Context context = movieAdapterViewHolder.mPosterImageView.getContext();
        Picasso.with(context).load(posterURL).into(movieAdapterViewHolder.mPosterImageView);
    }

    @Override
    public int getItemCount() {
        if (mMovieData == null) return 0;
        return mMovieData.length;
    }

    public void setMovieData(String[][] movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }

}
