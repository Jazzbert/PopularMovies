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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = "TMPDEBUG" + MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    public static final String[] MOVIE_LIST_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_POSTER = 2;

    private static final int ID_MOVIE_LOADER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

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

        getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);

        showLoading();

        MovieSyncUtils.initialize(this);

        showMovieDataView();

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
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        loadMovieData();
    }

    /*
     * Method required by AdapterView.OnItemSelectedListener
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    /*
     * This method shows gets URL for the movie list query and starts the async task to
     * fetch the data and populate the adapter.
     */
    private void loadMovieData() {
        int sortType = getSortType();

        showMovieDataView();


    }

    private int getSortType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sort_key = getString(R.string.pref_sort_order_key);
        String sort_default = getString(R.string.pref_sort_order_default);
        return Integer.parseInt(sharedPreferences.getString(sort_key, sort_default));
    }

    public void onClick(int movieID) {
        Class destinationClass = MovieDetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(this, destinationClass);
        Uri uriForMovieClicked = MovieContract.MovieEntry.buildMovieDetailUri(movieID);
        intentToStartDetailActivity.setData(uriForMovieClicked);
        intentToStartDetailActivity.putExtra(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieID);
        startActivity(intentToStartDetailActivity);

    }

    /* This method hides the error message and displays movie poster grid. */
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /* This method hides the movie poster grid and shows the error message. */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
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
                Uri movieQueryUri = MovieContract.MovieEntry.MOVIE_CONTENT_URI;

                Log.d(TAG, "MovieQueryUri: " + movieQueryUri.toString());

                return new CursorLoader(this,
                        movieQueryUri,
                        MOVIE_LIST_PROJECTION,
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
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showMovieDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

}