package com.craigcleveland.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by craig on 8/24/17.
 */

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.craigcleveland.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_POPULAR = "movie/popular";
    public static final String PATH_TOP_RATED = "movie/top_rated";
    public static final String PATH_TRAILERS = "videos";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri MOVIE_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE).build();
        public static final Uri TRAILER_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILERS).build();
        public static final Uri POPULAR_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULAR).build();
        public static final Uri TOP_RATED_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TOP_RATED).build();

        /* Movie Table */
        public static final String MOVIE_TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_SYNOPSIS = "synopsys";

        /* Trailer Table */
        public static final String TRAILER_TABLE_NAME = "trailers";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_TRAILER_NAME = "trailer_name";
        public static final String COLUMN_TRAILER_KEY = "trailer_key";
        public static final String COLUMN_TRAILER_SITE = "trailer_site";

        public static Uri buildMovieDetailUri(int movieID) {
            return MOVIE_CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieID)).build();
        }

        public static Uri buildTrailerListUri(int movieID) {
            return MOVIE_CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieID))
                    .appendPath(PATH_TRAILERS).build();
        }

    }


}
