package com.craigcleveland.popularmovies.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.craigcleveland.popularmovies.R;
import com.craigcleveland.popularmovies.data.MovieContract;
import com.craigcleveland.popularmovies.data.MovieProvider;
import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;

import java.net.URL;

/**
 * Syncs the movie data with TheMovieDB site using the ContentProvider.
 */

public class MovieSyncTask {

//    private static final String TAG = MovieSyncTask.class.getSimpleName();

    synchronized static void syncMovies(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sort_key = context.getString(R.string.pref_sort_order_key);
        String sort_default = context.getString(R.string.pref_sort_order_default);
        String sort_pref = sharedPreferences.getString(sort_key, sort_default);

        int sortType;
        switch (sort_pref) {
            case "0":
                sortType = MovieProvider.MOST_POPULAR;
                break;
            case "1":
                sortType = MovieProvider.TOP_RATED;
                break;
            default:
                return;  // No sync needed for Favorites

        }

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    sortType,
                    context.getString(R.string.tmdbAPIKey),
                    -1);

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] movieValues = MovieDBJsonUtils
                    .getMovieContentValuesFromJson(jsonMovieResponse);

            if (movieValues != null && movieValues.length != 0) {
                ContentResolver movieCR = context.getContentResolver();
                movieCR.delete(MovieContract.MovieEntry.MOVIE_CONTENT_URI, null, null);

                movieCR.bulkInsert(MovieContract.MovieEntry.MOVIE_CONTENT_URI, movieValues);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    synchronized public static void syncTrailers(Context context, int movie_id) {

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    MovieProvider.TRAILERS_LIST,
                    context.getString(R.string.tmdbAPIKey),
                    movie_id);

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] trailerValues = MovieDBJsonUtils
                    .getTrailerContentValuesFromJson(jsonMovieResponse);

            if (trailerValues != null && trailerValues.length != 0) {
                ContentResolver trailerCR = context.getContentResolver();
                trailerCR.delete(MovieContract.MovieEntry.TRAILER_CONTENT_URI,
                        null, null);

                trailerCR.bulkInsert(
                        MovieContract.MovieEntry.TRAILER_CONTENT_URI, trailerValues);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    synchronized public static void syncReviews(Context context, int movie_id) {

        try {
            URL movieRequestUrl = NetworkUtils.buildTMDBURL(
                    MovieProvider.REVIEWS_LIST,
                    context.getString(R.string.tmdbAPIKey),
                    movie_id);

            String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

            ContentValues[] reviewValues = MovieDBJsonUtils
                    .getReviewContentValuesFromJson(jsonMovieResponse);

            if (reviewValues != null && reviewValues.length != 0) {
                ContentResolver reviewCR = context.getContentResolver();
                reviewCR.delete(MovieContract.MovieEntry.REVIEWS_CONTENT_URI,
                        null, null);

                reviewCR.bulkInsert(
                        MovieContract.MovieEntry.REVIEWS_CONTENT_URI, reviewValues);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
