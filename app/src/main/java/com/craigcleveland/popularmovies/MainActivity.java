package com.craigcleveland.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

        Spinner sortOrderSpinner;
        sortOrderSpinner = (Spinner) findViewById(R.id.spinner_sort_order);

        /* Populate Spinner values */
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order_array, R.layout.spinner_text);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_text);
        sortOrderSpinner.setAdapter(spinnerAdapter);
        sortOrderSpinner.setSelection(MOST_POPULAR);
        sortOrderSpinner.setOnItemSelectedListener(this);


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

        loadMovieData(sortOrderSpinner.getSelectedItemPosition());

    }

    /*
     * This method re-loads movie data based on change in sort-type spinner.  It is
     * required by AdapterView.OnItemSelectedListener.
     */
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        loadMovieData(pos);
    }

    /*
     * Method required by AdapterView.OnItemSelectedListener
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    /*
     * This method shows gets URL for the movie list query and starts the async task to
     * fetch the data and populate the adapater.
     */
    private void loadMovieData(int sortType) {
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
