package com.craigcleveland.popularmovies.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.craigcleveland.popularmovies.data.MovieContract;

/**
 * This is part of structure for scheduled sync, which I don't think is necessary for this app.
 * Might further develop in the future.
 */

public class MovieSyncUtils {

    private static boolean sMovieSyncInitialized;

    synchronized public static void initialize(@NonNull final Context context) {

        if (sMovieSyncInitialized) return;

        sMovieSyncInitialized = true;

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri movieQueryUri = MovieContract.MovieEntry.MOVIE_CONTENT_URI;

                String[] projectionColumns = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};

                Cursor cursor = context.getContentResolver().query(
                        movieQueryUri,
                        projectionColumns,
                        null,
                        null,
                        null);

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                if (null != cursor) cursor.close();
            }
        });

        checkForEmpty.start();

    }

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, MovieSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

}
