package com.craigcleveland.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by craig on 8/23/17.
 */

public class MovieProvider extends ContentProvider {

    private static final String TAG = "CCDEBUG-" + MovieProvider.class.getSimpleName();


    // Public variables for use to reference from other classes
    public static final int MOVIE_LIST = 100;
    public static final int MOST_POPULAR = 101;
    public static final int TOP_RATED = 102;
    public static final int FAVORITES = 103;

    public static final String MOVIES_URI_STRING = MovieContract.PATH_MOVIE;
    public static final String FAVORITES_URI_STRING = MovieContract.PATH_FAVORITES;

    public static final int MOVIE_DETAIL = 200;
    public static final String MOVIE_DETAIL_URI_STRING = MovieContract.PATH_MOVIE + "/#";

    public static final int FAV_DETAIL = 201;
    public static final String FAV_DETAIL_URI_STRING = MovieContract.PATH_FAVORITES + "/#";

    public static final int TRAILERS_LIST = 300;
    public static final String TRAILERS_URI_STRING = MovieContract.PATH_TRAILERS;

    public static final int TRAILER_DETAIL = 400;
    public static final String TRAILER_DETAIL_URI_STRING = MovieContract.PATH_TRAILERS + "/#";

    public static final int REVIEWS_LIST = 500;
    public static final String REVIEW_URI_STRING = MovieContract.PATH_REVIEWS;

    public static final int REVIEW_DETAIL = 600;
    public static final String REVIEW_DETAIL_URI_STRING = MovieContract.PATH_REVIEWS + "/#";

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MOVIES_URI_STRING, MOVIE_LIST);
        matcher.addURI(authority, MOVIE_DETAIL_URI_STRING, MOVIE_DETAIL);
        matcher.addURI(authority, TRAILERS_URI_STRING, TRAILERS_LIST);
        matcher.addURI(authority, TRAILER_DETAIL_URI_STRING, TRAILER_DETAIL);
        matcher.addURI(authority, REVIEW_URI_STRING, REVIEWS_LIST);
        matcher.addURI(authority, REVIEW_DETAIL_URI_STRING, REVIEW_DETAIL);
        matcher.addURI(authority, FAVORITES_URI_STRING, FAVORITES);
        matcher.addURI(authority, FAV_DETAIL_URI_STRING, FAV_DETAIL);

        return matcher;

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.MOVIE_VIEW_NAME,
                        projection,
                        selection,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITES:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.FAVORITES_TABLE_NAME,
                        projection,
                        selection,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE_DETAIL:
                selectArgs = new String[]{uri.getLastPathSegment()};

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.MOVIE_VIEW_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectArgs,
                        null,
                        null,
                        sortOrder);

                // I believe this is necessary because of switching URIs on the cursor for
                // the same recycler view.
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;

            case FAV_DETAIL:
                selectArgs = new String[]{uri.getLastPathSegment()};

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.FAVORITES_TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;


            case TRAILERS_LIST:
                Log.d(TAG, "Query table name: " + MovieContract.MovieEntry.TRAILER_TABLE_NAME);

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TRAILER_TABLE_NAME,
                        projection,
                        selection,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TRAILER_DETAIL:
                String trailer_ID = uri.getLastPathSegment();
                selectArgs = new String[]{trailer_ID};

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TRAILER_TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_TRAILER_ID + " = ? ",
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case REVIEWS_LIST:
                Log.d(TAG, "Query table name: " + MovieContract.MovieEntry.REVIEW_TABLE_NAME);

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.REVIEW_TABLE_NAME,
                        projection,
                        selection,
                        selectArgs,
                        null,
                        null,
                        sortOrder);

                // I believe this is necessary because of switching URIs on the cursor for
                // the same recycler view.
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;

            case REVIEW_DETAIL:
                String review_ID = uri.getLastPathSegment();
                selectArgs = new String[]{review_ID};

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.REVIEW_TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_REVIEW_ID + " = ? ",
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;


            default:
                throw new UnsupportedOperationException( "Uri not recognized: " + uri.toString());
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        switch (sUriMatcher.match(uri)) {
            case FAVORITES:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                long _id = db.insert(MovieContract.MovieEntry.FAVORITES_TABLE_NAME,
                        null,
                        contentValues);
                if (_id == -1) {
                    Log.e(TAG, "Unable to make DB insert, URI: " + uri.toString());
                }

                Log.d(TAG, "Favorite row inserted at " + _id);

                return null;

            default:
                throw new UnsupportedOperationException("Uri not recognized: " + uri.toString());
        }

    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsInserted;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.MOVIE_TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case TRAILERS_LIST:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TRAILER_TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case REVIEWS_LIST:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.REVIEW_TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                throw new UnsupportedOperationException("Uri not recognized: " + uri.toString());

        }


    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;

        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.MOVIE_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case TRAILERS_LIST:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TRAILER_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case REVIEWS_LIST:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.REVIEW_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case FAVORITES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.FAVORITES_TABLE_NAME,
                        selection,
                        selectionArgs);
                Log.d(TAG, "Favorite rows deleted: " + numRowsDeleted);
                break;

            default:
                throw new UnsupportedOperationException("Uri not recognized: " + uri.toString());
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
