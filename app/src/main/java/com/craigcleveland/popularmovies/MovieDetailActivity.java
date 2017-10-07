package com.craigcleveland.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.sync.MovieSyncTask;
import com.craigcleveland.popularmovies.sync.MovieSyncUtils;
import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    private static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
    };

    public static final int INDEX_MOVIE_POSTER = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_RATING = 3;
    public static final int INDEX_MOVIE_SYNOPSIS = 4;
    
    public static final String[] TRAILER_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_TRAILER_ID,
            MovieContract.MovieEntry.COLUMN_TRAILER_KEY,
            MovieContract.MovieEntry.COLUMN_TRAILER_NAME
    };

    public static final int INDEX_TRAILER_ID = 0;
    public static final int INDEX_TRAILER_KEY = 1;
    public static final int INDEX_TRAILER_NAME = 2;
    
    private static final int ID_DETAIL_LOADER = 1200;
    private static final int TRAILER_LIST_LOADER = 1300;

    private static ImageView sPosterImageView;
    private static TextView sMovieTitleTextView;
    private static TextView sReleaseDateTextView;
    private static TextView sUserRatingTextView;
    private static TextView sSynopsisTextView;

    private static int sMovieID;

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        sMovieID = getIntent().getIntExtra(MovieContract.MovieEntry.COLUMN_MOVIE_ID, -1);

        sPosterImageView = (ImageView) findViewById(R.id.iv_poster);
        sMovieTitleTextView = (TextView) findViewById(R.id.tv_detail_title);
        sReleaseDateTextView = (TextView) findViewById(R.id.tv_detail_release_date);
        sUserRatingTextView = (TextView) findViewById(R.id.tv_detail_user_rating);
        sSynopsisTextView = (TextView) findViewById(R.id.tv_detail_synopsis);

        mUri = getIntent().getData();
        if (null == mUri) throw new NullPointerException("URI for DetailActivity cannot be null");


        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<Cursor> movieDetailLoader = loaderManager.getLoader(ID_DETAIL_LOADER);
        if (movieDetailLoader == null) {
            loaderManager.initLoader(ID_DETAIL_LOADER, null, this);
        } else {
            loaderManager.restartLoader(ID_DETAIL_LOADER, null, this);
        }

        Loader<Cursor> trailersLoader = loaderManager.getLoader(TRAILER_LIST_LOADER);
        if (trailersLoader == null) {
            loaderManager.initLoader(TRAILER_LIST_LOADER, null, this);
        } else {
            loaderManager.restartLoader(TRAILER_LIST_LOADER, null, this);
        }

        Toast.makeText(this, "MovieID: " + Integer.toString(sMovieID), Toast.LENGTH_LONG).show();




    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case ID_DETAIL_LOADER:
                Log.d("CCDEBUG " + TAG, "Getting detail data");
                return new CursorLoader(this,
                        mUri,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            case TRAILER_LIST_LOADER:
                Log.d("CCDEBUG " + TAG, "Beginning trailer load");
                return new AsyncTaskLoader<Cursor>(this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {
                        if (sMovieID <=0) return null;
                        MovieSyncTask.syncTrailers(getContext(), sMovieID);

                        return null;
                    }
                };

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;

        switch (loader.getId()) {
            case ID_DETAIL_LOADER:
                /* Check if we have valid data in the cursor */
                if (data != null && data.moveToFirst()) {
                    cursorHasValidData = true;
                }

                if (!cursorHasValidData) return;

                Log.d("CCDEBUG" + TAG, "Rows in detail cursor: " + data.getCount());

                // Set Poster
                String posterUrl =
                        NetworkUtils.buildPosterURL(data.getString(INDEX_MOVIE_POSTER));
                Picasso.with(this).load(posterUrl).into(sPosterImageView);

                // Set Remaining Text Items
                sMovieTitleTextView.setText(data.getString(INDEX_MOVIE_TITLE));
                sReleaseDateTextView.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
                sUserRatingTextView.setText(data.getString(INDEX_MOVIE_RATING));
                sSynopsisTextView.setText(data.getString(INDEX_MOVIE_SYNOPSIS));

                break;

            case TRAILER_LIST_LOADER:
                /* Check if we have valid data in the cursor */
                if (data != null && data.moveToFirst()) {
                    cursorHasValidData = true;
                }

                if (!cursorHasValidData) return;

                Log.d("CCDEBUG" + TAG, "Rows in trailer cursor: " + data.getCount());

                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    public class DetailDataLoader extends AsyncTaskLoader<Void> {
//
//        int mMovieID;
//
//
//        public DetailDataLoader(Context context, int movieID) {
//            super(context);
//            mMovieID = movieID;
//        }
//
//        @Override
//        public Void loadInBackground() {
//            Log.d("CCDEBUG " + TAG, "Starting trailer sync");
//            MovieSyncTask.syncTrailers(getContext(), mMovieID);
//            Log.d("CCDEBUG " + TAG, "Finished trailer sync");
//
//            return null;
//        }
//    }
//
}
