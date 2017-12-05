package com.craigcleveland.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.sync.MovieSyncUtils;

/*
 * Main activity for Popular Movies app displays a list of movie posters.  It opens
 * by default to "most popular" list from data from themoviedb.org API.
 */
public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

//    private final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    private int mPosition = RecyclerView.NO_POSITION;
    private static final String POSITION_KEY = "position_key";

    private TextView mErrorMessageDisplay;
    private TextView mNoFavoritesDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String[] MOVIE_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER
    };

    private static final String[] FAV_MOVIE_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_POSTER = 2;

    private static final int ID_MOVIE_LOADER = 100;
    private static final int ID_FAV_MOVIE_LOADER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rv_main);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mNoFavoritesDisplay = findViewById(R.id.tv_no_favorites_message);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        /* Using LinearLayoutManager to support different options later */
        GridLayoutManager layoutManger =
                new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManger);

        /*
         * Setting this to fixed size will improve performance.  All posters will be same
         * size.
         */
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this, this);
        mRecyclerView.setAdapter(mMovieAdapter);

        if (getSortType() == 2) {
            getSupportLoaderManager().initLoader(ID_FAV_MOVIE_LOADER, null, this);
        } else {
            getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
            showLoading();
            MovieSyncUtils.initialize(this);
            showMovieDataView();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION_KEY, mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_about:
                Intent startAboutActivity = new Intent(this, AboutActivity.class);
                startActivity(startAboutActivity);
                return true;
            case R.id.mi_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * This method re-loads movie data based on change in sort-type spinner.  It is
     * required by AdapterView.OnItemSelectedListener.
     */

    @Override
    protected void onResume() {
        if (getSortType() == 2) {
            getSupportLoaderManager().initLoader(ID_FAV_MOVIE_LOADER, null, this);
            //mRecyclerView.swapAdapter(mMovieAdapter, true);
        } else {
            getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
            showLoading();
            MovieSyncUtils.initialize(this);
            showMovieDataView();
            //mRecyclerView.swapAdapter(mMovieAdapter, true);
        }
        super.onResume();
    }

    private int getSortType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sort_key = getString(R.string.pref_sort_order_key);
        String sort_default = getString(R.string.pref_sort_order_default);
        return Integer.parseInt(sharedPreferences.getString(sort_key, sort_default));
    }

    public void onClick(int movieID, int position) {
        mPosition = position;
        Class destinationClass = MovieDetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(this, destinationClass);
        Uri uriForMovieClicked = MovieContract.MovieEntry.buildMovieDetailUri(movieID, getSortType());
        intentToStartDetailActivity.setData(uriForMovieClicked);
        intentToStartDetailActivity.putExtra(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieID);
        startActivity(intentToStartDetailActivity);
    }

    /* This method hides the error message and displays movie poster grid. */
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mNoFavoritesDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /* This method hides the movie poster grid and shows the error message. */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mNoFavoritesDisplay.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /* This method hides the movie poster grid and shows the message for no favorites selected. */
    private void showNoFavoritesMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mNoFavoritesDisplay.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /* This shows the loading indicator and hides the movie grid and error message */
    private void showLoading() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {
            case ID_MOVIE_LOADER:
                return new CursorLoader(this,
                        MovieContract.MovieEntry.MOVIE_CONTENT_URI,
                        MOVIE_LIST_PROJECTION,
                        null,
                        null,
                        null);

            case ID_FAV_MOVIE_LOADER:
                return new CursorLoader(this,
                        MovieContract.MovieEntry.FAVORITE_MOVIE_URI,
                        FAV_MOVIE_LIST_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null == data) throw new RuntimeException("No data returned from the loader");
        mMovieAdapter.swapCursor(data);
        mMovieAdapter.notifyDataSetChanged();
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) {
            showMovieDataView();
        } else {
            if (getSortType() == 2) {
                showNoFavoritesMessage();
            } else {
                showErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
        mMovieAdapter.notifyDataSetChanged();
    }

}
