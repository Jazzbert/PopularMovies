package com.craigcleveland.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Movie Contract defines table and column names for the movie database.  The contract is
 * intended to provide public references, though not all references may be used by the
 * current version of the application.
 */
@SuppressWarnings("WeakerAccess")
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.craigcleveland.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILERS = "videos";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_FAVORITES = "favorites";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri MOVIE_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE).build();
        public static final Uri TRAILER_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILERS).build();
        public static final Uri REVIEWS_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS).build();
        public static final Uri FAVORITE_MOVIE_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES).build();

        /* Movie Table */
        public static final String MOVIE_TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_SYNOPSIS = "synopsis";

        /* Trailer Table */
        public static final String TRAILER_TABLE_NAME = "trailers";
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_TRAILER_NAME = "trailer_name";
        public static final String COLUMN_TRAILER_KEY = "trailer_key";
        public static final String COLUMN_TRAILER_SITE = "trailer_site";

        /* Review Table */
        public static final String REVIEW_TABLE_NAME = "reviews";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_REVIEW_AUTHOR = "review_author";
        public static final String COLUMN_REVIEW_CONTENT = "review_content";

        /* Favorite Movies Table */
        public static final String FAVORITES_TABLE_NAME = "favorites";
        public static final String COLUMN_FAV_MOVIE_ID = "fav_movie_id";

        /* Movies View (for use in joined table query) */
        public static final String MOVIE_VIEW_NAME = "movie_view";

        public static Uri buildMovieDetailUri(int movieID, int sortType) {
            if (sortType == 2) {
                return FAVORITE_MOVIE_URI.buildUpon()
                        .appendPath(Integer.toString(movieID)).build();
            } else {
                return MOVIE_CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(movieID)).build();
            }
        }

    }


}
