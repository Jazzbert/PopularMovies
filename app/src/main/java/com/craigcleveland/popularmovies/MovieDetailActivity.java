package com.craigcleveland.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.sync.MovieSyncTask;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity implements
        TrailerAdapter.TrailerAdapterClickHandler,
        ReviewAdapter.ReviewAdapaterClickHandler,
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CCDEBUG-" + MovieDetailActivity.class.getSimpleName();

    private static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_FAV_MOVIE_ID
    };

    public static final int INDEX_MOVIE_POSTER = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_RATING = 3;
    public static final int INDEX_MOVIE_SYNOPSIS = 4;
    public static final int INDEX_MOVIE_FAVORITE_ID = 5;
    
    public static final String[] TRAILER_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_TRAILER_ID,
            MovieContract.MovieEntry.COLUMN_TRAILER_KEY,
            MovieContract.MovieEntry.COLUMN_TRAILER_NAME
    };

    public static final int INDEX_TRAILER_ID = 0;
    public static final int INDEX_TRAILER_KEY = 1;
    public static final int INDEX_TRAILER_NAME = 2;
    
    public static final String[] REVIEW_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_REVIEW_ID,
            MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR,
            MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT
    };

    public static final int INDEX_REVIEW_ID = 0;
    public static final int INDEX_REVIEW_AUTHOR = 1;
    public static final int INDEX_REVIEW_CONTENT = 2;

    private static final int ID_DETAIL_LOADER = 1200;
    public static final int TRAILER_LIST_LOADER = 1300;
    public static final int REVIEW_LIST_LOADER = 1400;

    private ImageView mPosterImageView;
    private TextView mMovieTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mUserRatingTextView;
    private TextView mSynopsisTextView;
    private ToggleButton mFavoriteToggleButton;

    private RecyclerView mTrailerRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private int mTrailerPosition = RecyclerView.NO_POSITION;

    private RecyclerView mReviewRecyclerView;
    private ReviewAdapter mReviewAdapter;
    private int mReviewPosition = RecyclerView.NO_POSITION;

    private static int sMovieID;
    private static String sTitle;
    private static String sPoster;
    private static String sReleaseDate;
    private static String sRating;
    private static String sSynopsis;

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
        mFavoriteToggleButton = (ToggleButton) findViewById(R.id.tb_favorite);

        mUri = getIntent().getData();
        if (null == mUri  ) throw new NullPointerException("URI for DetailActivity cannot be null");

        // Set toggle button change listener
        mFavoriteToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the provider based on if favorite is checked or not
                updateFavoriteMovie(sMovieID, isChecked);
            }
        });

        // Establish trailer layout items
        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this);
        mTrailerRecyclerView.setLayoutManager(trailerLayoutManager);
        mTrailerRecyclerView.setHasFixedSize(true);

        mTrailerAdapter = new TrailerAdapter(this, this);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);

        // Establish review layout items
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);

        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        mReviewRecyclerView.setLayoutManager(reviewLayoutManager);
        mReviewRecyclerView.setHasFixedSize(true);

        mReviewAdapter = new ReviewAdapter(this, this);
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        // Start data loads
        LoaderManager loaderManager = getSupportLoaderManager();

        runLoader(loaderManager, ID_DETAIL_LOADER, this);
        runLoader(loaderManager, TRAILER_LIST_LOADER, this);
        runLoader(loaderManager, REVIEW_LIST_LOADER, this);
    }

    private void runLoader(LoaderManager lm, int loaderID, LoaderManager.LoaderCallbacks callback) {
        Loader<Cursor> loader = lm.getLoader(loaderID);
        if (loader == null) {
            lm.initLoader(loaderID, null, callback);
        } else {
            lm.restartLoader(loaderID, null, callback);
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
                return new NewDataCursorLoader(this,
                        MovieContract.MovieEntry.TRAILER_CONTENT_URI,
                        TRAILER_LIST_PROJECTION);

            case REVIEW_LIST_LOADER:
                Log.d(TAG, "Beginning reviews load");
                return new NewDataCursorLoader(this,
                        MovieContract.MovieEntry.REVIEWS_CONTENT_URI,
                        REVIEW_LIST_PROJECTION);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);

        }
    }

    private static class NewDataCursorLoader extends AsyncTaskLoader<Cursor> {
        private Uri mContentUri;
        private String[] mProjection;

        public NewDataCursorLoader(Context context, Uri contentUri, String[] projection) {
            super(context);
            mContentUri = contentUri;
            mProjection = projection;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            if (sMovieID <=0) return null;
            Log.d(TAG, "Getting data for: " + mContentUri.toString());
            if (mContentUri == MovieContract.MovieEntry.TRAILER_CONTENT_URI) {
                MovieSyncTask.syncTrailers(getContext(), sMovieID);
            } else {
                MovieSyncTask.syncReviews(getContext(), sMovieID);
            }
            Log.d(TAG, "Completed data load for: " + mContentUri.toString());

            ContentResolver contentResolver = getContext().getContentResolver();
            Cursor result = contentResolver.query(mContentUri,
                    mProjection,
                    null,
                    null,
                    null);

            Log.d(TAG, "Cursor rows: " + Integer.toString(result.getCount()));

            return result;

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

                sTitle = data.getString(INDEX_MOVIE_TITLE);
                sPoster = data.getString(INDEX_MOVIE_POSTER);
                sReleaseDate = data.getString(INDEX_MOVIE_RELEASE_DATE);
                sRating = data.getString(INDEX_MOVIE_RATING);
                sSynopsis = data.getString(INDEX_MOVIE_SYNOPSIS);

                // Set Poster
                String posterUrl =
                        NetworkUtils.buildPosterURL(sPoster);
                Picasso.with(this).load(posterUrl).into(mPosterImageView);

                // Format and set release date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date relDate = null;
                Log.d(TAG, "Date trying to format: " + sReleaseDate);
                try {
                    relDate = sdf.parse(sReleaseDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                java.text.DateFormat df =
                        android.text.format.DateFormat.getMediumDateFormat(this);
                mReleaseDateTextView.setText(df.format(relDate));

                //Set Favorite flag
                if (data.getInt(INDEX_MOVIE_FAVORITE_ID) > 0)  {
                    mFavoriteToggleButton.setChecked(true);
                } else {
                    mFavoriteToggleButton.setChecked(false);
                }

                // Set Remaining Text Items
                mMovieTitleTextView.setText(sTitle);
                mUserRatingTextView.setText(sRating + " / 10");
                mSynopsisTextView.setText(sSynopsis);

                break;

            case TRAILER_LIST_LOADER:
                /* Check if we have valid data in the cursor */
                if (null == data) throw new RuntimeException("No data returned from the loader");
                mTrailerAdapter.swapCursor(data);
                Log.d(TAG, "Trailer data updated, total rows = " + data.getCount());
                if (mTrailerPosition == RecyclerView.NO_POSITION) mTrailerPosition = 0;
                break;

            case REVIEW_LIST_LOADER:
                if (null == data) throw new RuntimeException("No data returned from the loader");
                mReviewAdapter.swapCursor(data);
                Log.d(TAG, "Review data updated, total rows = " + data.getCount());
                if (mReviewPosition == RecyclerView.NO_POSITION) mReviewPosition = 0;
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
    public void onClick(String trailerID) {
        Intent watchVideo = new Intent(Intent.ACTION_VIEW);
        Uri videoUri = Uri.parse("http://www.youtube.com/embed/" + trailerID);
        watchVideo.setData(videoUri);
        if (watchVideo.resolveActivity(getPackageManager()) != null) {
            startActivity(watchVideo);
        }
    }

    @Override
    public void onClick(String reviewAuthor, String reviewContent) {
        Intent readReview = new Intent(this, ReviewActivity.class);
        readReview.putExtra(MovieContract.MovieEntry.COLUMN_TITLE,
                mMovieTitleTextView.getText());
        readReview.putExtra(MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR,
                reviewAuthor);
        readReview.putExtra(MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT,
                reviewContent);
        startActivity(readReview);
    }


    private void updateFavoriteMovie (final int movieID, final boolean isFavorite) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFavorite) {
                    ContentValues cv = new ContentValues();
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_MOVIE_ID, movieID);
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_TITLE, sTitle);
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_POSTER, sPoster);
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_RELEASE_DATE, sReleaseDate);
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_RATING, sRating);
                    cv.put(MovieContract.MovieEntry.COLUMN_FAV_SYNOPSIS, sSynopsis);
                    getContentResolver().insert(
                            MovieContract.MovieEntry.FAVORITE_MOVIE_URI,
                            cv);
                } else {
                    getContentResolver().delete(
                            MovieContract.MovieEntry.FAVORITE_MOVIE_URI,
                            MovieContract.MovieEntry.COLUMN_FAV_MOVIE_ID + " = ?",
                            new String[] {Integer.toString(movieID)});
                }

            }
        }).start();
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
