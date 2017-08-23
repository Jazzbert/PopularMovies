package com.craigcleveland.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;

import java.net.URL;

/*
 * Main activity for Popular Movies app displays a list of movie posters.  It opens
 * by default to "most popular" list from data from themoviedb.org API.
 */
public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieAdapterClickHandler, AdapterView.OnItemSelectedListener {

    // Public variables for use to reference from other classes
    @SuppressWarnings("WeakerAccess")
    public static final int MOST_POPULAR = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int HIGHEST_RATED = 1;

    //private Spinner mSortOrderSpinner;
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

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

        mMovieAdapter = new MovieAdapter(this);

        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData();

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sort_key = getString(R.string.pref_sort_order_key);
        String sort_default = getString(R.string.pref_sort_order_default);
        int sortType = Integer.parseInt(sharedPreferences.getString(sort_key, sort_default));

        showMovieDataView();

        URL fetchURL = NetworkUtils.buildUrl(sortType, getString(R.string.tmdbAPIKey));
        new FetchMovieTask().execute(fetchURL);

    }

    public void onClick(String[] clickedMovieDetails) {
        Class destinationClass = MovieDetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(this, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, clickedMovieDetails);
        startActivity(intentToStartDetailActivity);

    }

    /*
     * This method hides the error message and displays movie poster grid.
     */
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /*
     * This method hides the movie poster grid and shows the error message.
     */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private class FetchMovieTask extends AsyncTask<URL, Void, String[][]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[][] doInBackground(URL... params) {

            /* If there's no URL, nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            URL requestURL = params[0];

            try {
                String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(requestURL);

                return MovieDBJsonUtils.getSimpleMovieStringsFromJson(jsonMovieResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[][] movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();
                mMovieAdapter.setMovieData(movieData);
            } else {
                showErrorMessage();
            }
        }

    }
}
