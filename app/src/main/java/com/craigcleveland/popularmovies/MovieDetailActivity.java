package com.craigcleveland.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    private static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS
    };

    private static final int INDEX_MOVIE_POSTER = 0;
    private static final int INDEX_MOVIE_TITLE = 1;
    private static final int INDEX_MOVIE_RELEASE_DATE = 2;
    private static final int INDEX_MOVIE_RATING = 3;
    private static final int INDEX_MOVIE_SYNOPSIS = 4;

    private static final int ID_DETAIL_LOADER = 200;

    private static ImageView sPosterImageView;
    private static TextView sMovieTitleTextView;
    private static TextView sReleaseDateTextView;
    private static TextView sUserRatingTextView;
    private static TextView sSynopsisTextView;

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);



        String[] movieDetails;

        sPosterImageView = (ImageView) findViewById(R.id.iv_poster);
        sMovieTitleTextView = (TextView) findViewById(R.id.tv_detail_title);
        sReleaseDateTextView = (TextView) findViewById(R.id.tv_detail_release_date);
        sUserRatingTextView = (TextView) findViewById(R.id.tv_detail_user_rating);
        sSynopsisTextView = (TextView) findViewById(R.id.tv_detail_synopsis);

        mUri = getIntent().getData();
        if (null == mUri) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_DETAIL_LOADER:
                return new CursorLoader(this,
                        mUri,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /* Check if we have valid data in the cursor */
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) return;

        Log.d("CCDEBUG" + TAG, "Rows in cursor: " + data.getCount());

        // Set Poster
        String posterUrl =
                NetworkUtils.buildPosterURL(data.getString(INDEX_MOVIE_POSTER));
        Picasso.with(this).load(posterUrl).into(sPosterImageView);

        // Set Remaining Text Items
        sMovieTitleTextView.setText(data.getString(INDEX_MOVIE_TITLE));
        sReleaseDateTextView.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
        sUserRatingTextView.setText(data.getString(INDEX_MOVIE_RATING));
        sSynopsisTextView.setText(data.getString(INDEX_MOVIE_SYNOPSIS));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
