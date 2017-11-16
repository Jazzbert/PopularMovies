package com.craigcleveland.popularmovies;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.sync.MovieSyncTask;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity implements
        TrailerAdapter.TrailerAdapterClickHandler,
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CCDEBUG-" + MovieDetailActivity.class.getSimpleName();

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

    private ImageView mPosterImageView;
    private TextView mMovieTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mUserRatingTextView;
    private TextView mSynopsisTextView;

    private RecyclerView mTrailerRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private int mTrailerPosition = RecyclerView.NO_POSITION;

    private static int sMovieID;

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        sMovieID = getIntent().getIntExtra(MovieContract.MovieEntry.COLUMN_MOVIE_ID, -1);

        // Establish basic movie detail items
        mPosterImageView = (ImageView) findViewById(R.id.iv_poster);
        mMovieTitleTextView = (TextView) findViewById(R.id.tv_detail_title);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_detail_release_date);
        mUserRatingTextView = (TextView) findViewById(R.id.tv_detail_user_rating);
        mSynopsisTextView = (TextView) findViewById(R.id.tv_detail_synopsis);

        mUri = getIntent().getData();
        if (null == mUri) throw new NullPointerException("URI for DetailActivity cannot be null");

        // Establish trailer layout items
        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this);
        mTrailerRecyclerView.setLayoutManager(trailerLayoutManager);
        mTrailerRecyclerView.setHasFixedSize(true);

        mTrailerAdapter = new TrailerAdapter(this, this);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);

        // Start data loads
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


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case ID_DETAIL_LOADER:
                Log.d(TAG, "Getting detail data");
                return new CursorLoader(this,
                        mUri,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            case TRAILER_LIST_LOADER:
                Log.d(TAG, "Beginning trailer load");
                return new AsyncTaskLoader<Cursor>(this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {
                        if (sMovieID <=0) return null;
                        Log.d(TAG, "Getting trailer data from Internet");
                        MovieSyncTask.syncTrailers(getContext(), sMovieID);
                        Log.d(TAG, "Completed trailer data load");

                        Uri trailerUri = MovieContract.MovieEntry.TRAILER_CONTENT_URI;
                        Log.d(TAG, "TrailerURI: " + trailerUri.toString());
                        ContentResolver trailerCR = getContext().getContentResolver();
                        Cursor result = trailerCR.query(trailerUri,
                                MovieDetailActivity.TRAILER_LIST_PROJECTION,
                                null,
                                null,
                                null);

                        Log.d(TAG, "Trailer Cursor rows: " + Integer.toString(result.getCount()));

                        return result;

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

                Log.d(TAG, "Rows in detail cursor: " + data.getCount());

                // Set Poster
                String posterUrl =
                        NetworkUtils.buildPosterURL(data.getString(INDEX_MOVIE_POSTER));
                Picasso.with(this).load(posterUrl).into(mPosterImageView);

                // Set Remaining Text Items
                mMovieTitleTextView.setText(data.getString(INDEX_MOVIE_TITLE));
                mReleaseDateTextView.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
                mUserRatingTextView.setText(data.getString(INDEX_MOVIE_RATING));
                mSynopsisTextView.setText(data.getString(INDEX_MOVIE_SYNOPSIS));

                break;

            case TRAILER_LIST_LOADER:
                /* Check if we have valid data in the cursor */
                if (null == data) throw new RuntimeException("No data returned from the loader");
                mTrailerAdapter.swapCursor(data);
                if (mTrailerPosition == RecyclerView.NO_POSITION) mTrailerPosition = 0;
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // Do nothing
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
    }

    @Override
    public void onClick(String trailerKey) {
        Intent watchVideo = new Intent(Intent.ACTION_VIEW);
        Uri videoUri = Uri.parse("http://www.youtube.com/embed/" + trailerKey);
        watchVideo.setData(videoUri);
        if (watchVideo.resolveActivity(getPackageManager()) != null) {
            startActivity(watchVideo);
        }
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
//            Log.d(TAG, "Starting trailer sync");
//            MovieSyncTask.syncTrailers(getContext(), mMovieID);
//            Log.d(TAG, "Finished trailer sync");
//
//            return null;
//        }
//    }
//
}
