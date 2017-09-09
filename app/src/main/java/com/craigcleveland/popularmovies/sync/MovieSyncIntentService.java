package com.craigcleveland.popularmovies.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by craig on 8/26/17.
 */

public class MovieSyncIntentService extends IntentService {

    public MovieSyncIntentService() { super("MovieSyncIntentService");}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MovieSyncTask.syncMovies(this);
    }
}
