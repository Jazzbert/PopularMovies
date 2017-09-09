package com.craigcleveland.popularmovies.utilities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.craigcleveland.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDBJsonUtils {

    private static final String TAG = MovieDBJsonUtils.class.getSimpleName();

    /* Movie Data Structure */
    public static final int MOVIE_ID = 0;
    public static final int MOVIE_TITLE = 1;
    public static final int MOVIE_POSTER = 2;
    public static final int MOVIE_RATING = 3;
    public static final int MOVIE_RELEASE = 4;
    public static final int MOVIE_SYNOPSIS = 5;

    /* Trailer Data Structure */
    public static final int TRAILER_NAME = 0;
    public static final int TRAILER_KEY = 1;
    public static final int TRAILER_SITE = 2;
    public static final int TRAILER_TYPE = 3;

    /* Movie field information */
    private static final String TMD_RESULTS_LABEL = "results";

    private static final String MOVIE_ID_LABEL = "id";
    private static final String MOVIE_TITLE_LABEL = "title";
    private static final String MOVIE_POSTER_LABEL = "poster_path";
    private static final String MOVIE_USER_RATING_LABEL = "vote_average";
    private static final String MOVIE_RELEASE_DATE_LABEL = "release_date";
    private static final String MOVIE_SYNOPSIS_LABEL = "overview";

    private static final String TRAILER_NAME_LABEL = "name";
    private static final String TRAILER_KEY_LABEL = "key";
    private static final String TRAILER_SITE_LABEL = "site";
    private static final String TRAILER_TYPE_LABEL = "trailer";

    private static final String TMD_CODE_LABEL = "status_code";
    private static final String TMD_MESSAGE_LABEL = "status_message";

    public static ContentValues[] getMovieContentValuesFromJson(Context context,
                                                                  String movieJsonStr)
            throws JSONException {
        JSONObject movieJson = new JSONObject(movieJsonStr);

        if (checkResponseError(movieJson)) throw new UnsupportedOperationException();

        JSONArray jsonMovieArray = movieJson.getJSONArray(TMD_RESULTS_LABEL);

        ContentValues[] movieContentValues = new ContentValues[jsonMovieArray.length()];

        for (int i = 0; i < jsonMovieArray.length(); i++) {
            JSONObject movie = jsonMovieArray.getJSONObject(i);
            // Movie ID
            int movieID = movie.getInt(MOVIE_ID_LABEL);

            // Movie Title
            String movieTitle = movie.getString(MOVIE_TITLE_LABEL);

            // Movie Poster Path
            String moviePoster = movie.getString(MOVIE_POSTER_LABEL);

            // Movie User Rating

            double movieRating = movie.getDouble(MOVIE_USER_RATING_LABEL);

            // Movie Release Date
            String movieReleaseDate = movie.getString(MOVIE_RELEASE_DATE_LABEL);

            // Movie Synopsis
            String movieSynopsis = movie.getString(MOVIE_SYNOPSIS_LABEL);


            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieID);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieTitle);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, moviePoster);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, movieRating);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
            movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movieSynopsis);

            movieContentValues[i] = movieValues;

        }

        return movieContentValues;

    }

    public static String[][] getSimpleMovieStringsFromJson(String movieJsonStr)
            throws JSONException {

        String[][] movieData;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Check for error */
        if (checkResponseError(movieJson)) throw new UnsupportedOperationException();

        JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS_LABEL);
        movieData = new String[movieArray.length()][6];

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);
            // Movie ID
            movieData[i][MOVIE_ID] = Integer.toString(movie.getInt(MOVIE_ID_LABEL));

            // Movie Title
            movieData[i][MOVIE_TITLE] = movie.getString(MOVIE_TITLE_LABEL);

            // Movie Poster Path
            movieData[i][MOVIE_POSTER] = movie.getString(MOVIE_POSTER_LABEL);

            // Movie User Rating
            DecimalFormat df = new DecimalFormat("#.0");
            movieData[i][MOVIE_RATING] = df.format(movie.getDouble(MOVIE_USER_RATING_LABEL));

            // Movie Release Date
            String reformattedDate = "";
            try {
                /* Specified Date Format based on input from themoviedb.org */
                @SuppressLint("SimpleDateFormat") DateFormat sdfIn =
                        new SimpleDateFormat("yyyy-MM-dd");
                DateFormat sdfOut = DateFormat.getDateInstance(DateFormat.MEDIUM);
                String originalDate = movie.getString(MOVIE_RELEASE_DATE_LABEL);
                reformattedDate = sdfOut.format(sdfIn.parse(originalDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            movieData[i][MOVIE_RELEASE] = reformattedDate;

            // Movie Synopsis
            movieData[i][MOVIE_SYNOPSIS] = movie.getString(MOVIE_SYNOPSIS_LABEL);
        }

        return movieData;
    }

    public static String[][] getSimpleTrailerStringsFromJson(String trailerJsonStr)
            throws JSONException {

        /* Trailer information.  Each trailer is an element of the "results" array */

        String[][] trailerData;

        JSONObject trailerJson = new JSONObject(trailerJsonStr);

        /* Check for error */
        if (checkResponseError(trailerJson)) throw new UnsupportedOperationException();

        JSONArray trailerArray = trailerJson.getJSONArray(TMD_RESULTS_LABEL);
        trailerData = new String[trailerArray.length()][4];

        for (int i = 0; i < trailerArray.length(); i++) {
            JSONObject trailer = trailerArray.getJSONObject(i);
            // Trailer Name
            trailerData[i][TRAILER_NAME] = trailer.getString(TRAILER_NAME_LABEL);

            // Trailer Key
            trailerData[i][TRAILER_KEY] = trailer.getString(TRAILER_KEY_LABEL);

            // Trailer Site
            trailerData[i][TRAILER_SITE] = trailer.getString(TRAILER_SITE_LABEL);

            // Trailer Type
            trailerData[i][TRAILER_TYPE] = trailer.getString(TRAILER_TYPE_LABEL);
        }

        return trailerData;

    }

    private static boolean checkResponseError(JSONObject jsonData) throws JSONException {
        if (jsonData.has(TMD_CODE_LABEL)) {
            int errorCode = jsonData.getInt(TMD_CODE_LABEL);
            switch (errorCode) {
                case 7: // Bad API key
                    Log.e(TAG, jsonData.getString(TMD_MESSAGE_LABEL));
                    return true;
                case 34: // Request could not be found (malformed request)
                    Log.e(TAG, jsonData.getString(TMD_MESSAGE_LABEL));
                    return true;
            }
        }
        return false;
    }

}