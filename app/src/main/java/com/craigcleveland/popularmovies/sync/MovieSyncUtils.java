package com.craigcleveland.popularmovies.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.craigcleveland.popularmovies.data.MovieContract;

/**
 * Created by craig on 8/26/17.
 */

public class MovieSyncUtils {

    private static boolean sMovieSyncInitialized;
    private static boolean sTrailerSyncInitialized;

    synchronized public static void initialize(@NonNull final Context context) {

        if (sMovieSyncInitialized) return;

        sMovieSyncInitialized = true;

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri movieQueryUri = MovieContract.MovieEntry.MOVIE_CONTENT_URI;

                String[] projectionColumns = {MovieContract.MovieEntry._ID};

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

    synchronized public static void syncTrailerList(@NonNull final Context context) {
        if (sTrailerSyncInitialized) return;

        sTrailerSyncInitialized = true;



    }

}
