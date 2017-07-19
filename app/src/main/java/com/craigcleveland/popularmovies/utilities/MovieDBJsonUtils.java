package com.craigcleveland.popularmovies.utilities;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MovieDBJsonUtils {

    private static final String TAG = MovieDBJsonUtils.class.getSimpleName();

    @SuppressWarnings("WeakerAccess") // For future use from other classes
    public static final int ID_ITEM = 0;
    public static final int TITLE_ITEM = 1;
    public static final int POSTER_ITEM = 2;
    public static final int RATING_ITEM = 3;
    public static final int RELEASE_ITEM = 4;
    public static final int SYNOPSIS_ITEM = 5;

    public static String[][] getSimpleMovieStringsFromJson(String movieJsonStr)
        throws JSONException {

        /* Movie information. Each Movie is an element of the "results" array */
        final String TMD_RESULTS = "results";

        final String TMD_ID = "id";
        final String TMD_TITLE = "title";
        final String TMD_POSTER = "poster_path";
        final String TMD_USER_RATING = "vote_average";
        final String TMD_RELEASE_DATE = "release_date";
        final String TMD_SYNOPSIS = "overview";

        final String TMD_CODE = "status_code";
        final String TMD_MESSAGE = "status_message";

        String[][] movieData;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Check for error */
        if (movieJson.has(TMD_CODE)) {
            int errorCode = movieJson.getInt(TMD_CODE);

            switch (errorCode) {
                case 7: // Bad API key
                    Log.e(TAG, movieJson.getString(TMD_MESSAGE));
                    return null;
                case 34: // Request could not be found (malformed request)
                    Log.e(TAG, movieJson.getString(TMD_MESSAGE));
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);

        movieData = new String[movieArray.length()][6];

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);
            // Movie ID
            movieData[i][ID_ITEM] = Integer.toString(movie.getInt(TMD_ID));

            // Movie Title
            movieData[i][TITLE_ITEM] = movie.getString(TMD_TITLE);

            // Movie Poster Path
            movieData[i][POSTER_ITEM] = movie.getString(TMD_POSTER);

            // Movie User Rating
            DecimalFormat df = new DecimalFormat("#.0");
            movieData[i][RATING_ITEM] = df.format(movie.getDouble(TMD_USER_RATING));

            // Movie Release Date
            String reformattedDate = "";
            try {
                /* Specified Date Format based on input from themoviedb.org */
                @SuppressLint("SimpleDateFormat") DateFormat sdfIn =
                        new SimpleDateFormat("yyyy-MM-dd");
                DateFormat sdfOut = DateFormat.getDateInstance(DateFormat.MEDIUM);
                String originalDate = movie.getString(TMD_RELEASE_DATE);
                reformattedDate = sdfOut.format(sdfIn.parse(originalDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            movieData[i][RELEASE_ITEM] = reformattedDate;

            // Movie Synopsis
            movieData[i][SYNOPSIS_ITEM] = movie.getString(TMD_SYNOPSIS);
        }

        return movieData;
    }
}
