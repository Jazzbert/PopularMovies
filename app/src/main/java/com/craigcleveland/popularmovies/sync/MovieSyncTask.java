package com.craigcleveland.popularmovies.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.craigcleveland.popularmovies.R;
import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.data.MovieProvider;
import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;

import java.net.URL;

/**
 * Created by craig on 8/26/17.
 */

public class MovieSyncTask {

    private static final String TAG = "CCDEBUG-" + MovieSyncTask.class.getSimpleName();

    synchronized public static void syncMovies(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sort_key = context.getString(R.string.pref_sort_order_key);
        String sort_default = context.getString(R.string.pref_sort_order_default);
        String sort_pref = sharedPreferences.getString(sort_key, sort_default);

        Log.d(TAG, "Sort preference retrieved: " + sort_pref);

        int sortType;
        if (sort_pref.equals("0")) {
            sortType = MovieProvider.MOST_POPULAR;
        } else if (sort_pref.equals("1")) {
            sortType = MovieProvider.TOP_RATED;
        } else {
            return;  // No sync needed for Favorites
        }

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    sortType,
                    context.getString(R.string.tmdbAPIKey),
                    -1);

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] movieValues = MovieDBJsonUtils
                    .getMovieContentValuesFromJson(context, jsonMovieResponse);

            Log.d(TAG, "movieValues length = " + movieValues.length);

            if (movieValues != null && movieValues.length != 0) {
                ContentResolver movieCR = context.getContentResolver();
                int numRowsDeleted = movieCR.delete(MovieContract.MovieEntry.MOVIE_CONTENT_URI, null, null);
                Log.d(TAG, "rows deleted = " + numRowsDeleted);

                int numRowsInserted = movieCR.bulkInsert(MovieContract.MovieEntry.MOVIE_CONTENT_URI, movieValues);
                Log.d(TAG, "rows inserted = " + numRowsInserted);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    synchronized public static void syncTrailers(Context context, int movie_id) {

        Log.d(TAG, "syncTrailers has started");

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    MovieProvider.TRAILERS_LIST,
                    context.getString(R.string.tmdbAPIKey),
                    movie_id);

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] trailerValues = MovieDBJsonUtils
                    .getTrailerContentValuesFromJson(context, jsonMovieResponse);

            if (trailerValues != null && trailerValues.length != 0) {
                ContentResolver trailerCR = context.getContentResolver();
                int numRowsDeleted = trailerCR.delete(MovieContract.MovieEntry.TRAILER_CONTENT_URI,
                        null, null);
                Log.d(TAG, "trailer rows deleted = " + numRowsDeleted);

                int numRowsInserted = trailerCR.bulkInsert(
                        MovieContract.MovieEntry.TRAILER_CONTENT_URI, trailerValues);
                Log.d(TAG, "trailer rows inserted = " + numRowsInserted);
                Log.d(TAG, "inserted at URI: " + MovieContract.MovieEntry.TRAILER_CONTENT_URI);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    synchronized public static void syncReviews(Context context, int movie_id) {

        Log.d(TAG, "syncReviews has started - really");

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    MovieProvider.REVIEWS_LIST,
                    context.getString(R.string.tmdbAPIKey),
                    movie_id);
            Log.d(TAG, "review url: " + movieRequestUrl.toString());

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] reviewValues = MovieDBJsonUtils
                    .getReviewContentValuesFromJson(context, jsonMovieResponse);

            if (reviewValues != null && reviewValues.length != 0) {
                ContentResolver reviewCR = context.getContentResolver();
                int numRowsDeleted = reviewCR.delete(MovieContract.MovieEntry.REVIEWS_CONTENT_URI,
                        null, null);
                Log.d(TAG, "review rows deleted = " + numRowsDeleted);

                int numRowsInserted = reviewCR.bulkInsert(
                        MovieContract.MovieEntry.REVIEWS_CONTENT_URI, reviewValues);
                Log.d(TAG, "review rows inserted = " + numRowsInserted);
                Log.d(TAG, "inserted at URI: " + MovieContract.MovieEntry.REVIEWS_CONTENT_URI);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
