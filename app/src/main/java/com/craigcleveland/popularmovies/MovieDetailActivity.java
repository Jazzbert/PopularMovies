package com.craigcleveland.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.craigcleveland.popularmovies.utilities.MovieDBJsonUtils;
import com.craigcleveland.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ImageView posterImageView;
        TextView movieTitleTextView;
        TextView releaseDateTextView;
        TextView userRatingTextView;
        TextView synopsisTextView;

        String[] movieDetails;

        posterImageView = (ImageView) findViewById(R.id.iv_poster);
        movieTitleTextView = (TextView) findViewById(R.id.tv_detail_title);
        releaseDateTextView = (TextView) findViewById(R.id.tv_detail_release_date);
        userRatingTextView = (TextView) findViewById(R.id.tv_detail_user_rating);
        synopsisTextView = (TextView) findViewById(R.id.tv_detail_synopsis);

        Intent intentThatStartedThis = getIntent();

        if (intentThatStartedThis != null) {
            if (intentThatStartedThis.hasExtra(Intent.EXTRA_TEXT)) {
                movieDetails = intentThatStartedThis.getStringArrayExtra(Intent.EXTRA_TEXT);

                // Set Poster
                String posterUrl =
                        NetworkUtils.buildPosterURL(movieDetails[MovieDBJsonUtils.MOVIE_POSTER]);
                Picasso.with(this).load(posterUrl).into(posterImageView);

                // Set Remaining Text Items
                movieTitleTextView.setText(movieDetails[MovieDBJsonUtils.MOVIE_TITLE]);
                releaseDateTextView.setText(movieDetails[MovieDBJsonUtils.MOVIE_RELEASE]);
                userRatingTextView.setText(movieDetails[MovieDBJsonUtils.MOVIE_RATING]);
                synopsisTextView.setText(movieDetails[MovieDBJsonUtils.MOVIE_SYNOPSIS]);


            }
        }
    }
}
