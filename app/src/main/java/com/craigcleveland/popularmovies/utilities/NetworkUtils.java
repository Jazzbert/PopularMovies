package com.craigcleveland.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import com.craigcleveland.popularmovies.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIE_DB_URL =
            "http://api.themoviedb.org/3";

    private static final String API_PARAM = "api_key";  // Keeping global for future use.

    /**
     *  Builds the URL used to talk to The Movie DB.  For now just returns popular results
     *
     * @return The URL to use to query the weather server
     */
    public static URL buildUrl(int sortType, String api_key) {

        String queryPath;
        if (sortType == MainActivity.HIGHEST_RATED) {
            queryPath = "movie/top_rated";
        } else {
            queryPath = "movie/popular";
        }

        Uri builtUri = Uri.parse(MOVIE_DB_URL).buildUpon()
                .appendEncodedPath(queryPath)
                .appendQueryParameter(API_PARAM, api_key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static String buildPosterURL(String posterPath) {
        return "http://image.tmdb.org/t/p/w185" + posterPath;
    }

}
