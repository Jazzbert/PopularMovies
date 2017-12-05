package com.craigcleveland.popularmovies.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * This subclass is structured to set up scheduled syncs; though, for this app may
 * I don't think it's really necessary.  Maybe will update in the future.
 */

public class MovieSyncIntentService extends IntentService {

    public MovieSyncIntentService() { super("MovieSyncIntentService");}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MovieSyncTask.syncMovies(this);
    }
}
