package com.craigcleveland.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.craigcleveland.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by craig on 8/24/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private final String TAG = "CCDEBUG" + MovieDbHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "movie.db";

    private static final int DATABASE_VERSION = 8;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieEntry.MOVIE_TABLE_NAME + " (" +
                MovieEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID  + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TITLE     + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER    + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING    + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS  + " TEXT NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_TRAILERS_TABLE =
                "CREATE TABLE " + MovieEntry.TRAILER_TABLE_NAME + " (" +
                MovieEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TRAILER_ID    + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TRAILER_NAME  + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TRAILER_KEY   + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TRAILER_SITE  + " TEXT NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_TRAILER_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " + MovieEntry.REVIEW_TABLE_NAME + " (" +
                MovieEntry._ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_REVIEW_ID      + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_REVIEW_AUTHOR  + " TEXT, " +
                MovieEntry.COLUMN_REVIEW_CONTENT + " TEXT, " +
                "UNIQUE (" + MovieEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_FAVORITES_TABLE =
                "CREATE TABLE " + MovieEntry.FAVORITES_TABLE_NAME + " (" +
                MovieEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_FAV_MOVIE_ID  + " INTEGER NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_FAV_MOVIE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_MOVIE_VIEW =
                "CREATE VIEW " + MovieEntry.MOVIE_VIEW_NAME + " AS " +
                "SELECT " + MovieEntry.COLUMN_MOVIE_ID      + ", " +
                MovieEntry.COLUMN_TITLE         + ", " +
                MovieEntry.COLUMN_POSTER        + ", " +
                MovieEntry.COLUMN_RATING        + ", " +
                MovieEntry.COLUMN_RELEASE_DATE  + ", " +
                MovieEntry.COLUMN_SYNOPSIS      + ", " +
                MovieEntry.FAVORITES_TABLE_NAME + "." + MovieEntry.COLUMN_FAV_MOVIE_ID + " " +
                "FROM " + MovieEntry.MOVIE_TABLE_NAME + " " +
                "LEFT JOIN " + MovieEntry.FAVORITES_TABLE_NAME + " ON " +
                MovieEntry.FAVORITES_TABLE_NAME + "." + MovieEntry.COLUMN_FAV_MOVIE_ID + " = " +
                MovieEntry.MOVIE_TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + ";";

//        " (" +
//                MovieEntry.COLUMN_MOVIE_ID      + ", " +
//                MovieEntry.COLUMN_TRAILER_ID    + ", " +
//                MovieEntry.COLUMN_TRAILER_NAME  + ", " +
//                MovieEntry.COLUMN_TRAILER_KEY   + ", " +
//                MovieEntry.COLUMN_FAVORITES_ID  +

//        MovieEntry.COLUMN_FAVORITES_ID  + ") AS " +
//        MovieEntry.FAVORITES_TABLE_NAME + "." + MovieEntry._ID + " " +

//        MovieEntry.COLUMN_ISFAVORITE  + ") AS " +
//                "ifnull(min(1, " +
//                MovieEntry.FAVORITES_TABLE_NAME + "." + MovieEntry._ID + "), 0) " +

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_VIEW);

        Log.d(TAG, "Finished running DB Updates");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " + MovieEntry.MOVIE_VIEW_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.MOVIE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TRAILER_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.REVIEW_TABLE_NAME);
        // This will eliminate favorites saved when upgrading DB...possibile future
        // enhancement may include code to retain current favorites during DB changes.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.FAVORITES_TABLE_NAME);
        onCreate(sqLiteDatabase);

        Log.d(TAG, "Finished cleaning up old DB version");

    }
}
