package com.craigcleveland.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.craigcleveland.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by craig on 8/24/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "movie.db";

    private static final int DATABASE_VERSION = 3;

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

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.MOVIE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}