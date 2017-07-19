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
        TextView userRatingTextView;
        TextView releaseDateTextView;
        TextView synopsisTextView;

        String[] movieDetails;

        posterImageView = (ImageView) findViewById(R.id.iv_poster);
        movieTitleTextView = (TextView) findViewById(R.id.tv_detail_title);
        userRatingTextView = (TextView) findViewById(R.id.tv_detail_user_rating);
        releaseDateTextView = (TextView) findViewById(R.id.tv_detail_release_date);
        synopsisTextView = (TextView) findViewById(R.id.tv_detail_synopsis);

        Intent intentThatStartedThis = getIntent();

        if (intentThatStartedThis != null) {
            if (intentThatStartedThis.hasExtra(Intent.EXTRA_TEXT)) {
                movieDetails = intentThatStartedThis.getStringArrayExtra(Intent.EXTRA_TEXT);

                // Set Poster
                String posterUrl =
                        NetworkUtils.buildPosterURL(movieDetails[MovieDBJsonUtils.POSTER_ITEM]);
                Picasso.with(this).load(posterUrl).into(posterImageView);

                // Set Remaining Text Items
                movieTitleTextView.setText(movieDetails[MovieDBJsonUtils.TITLE_ITEM]);
                userRatingTextView.setText(movieDetails[MovieDBJsonUtils.RATING_ITEM]);
                releaseDateTextView.setText(movieDetails[MovieDBJsonUtils.RELEASE_ITEM]);
                synopsisTextView.setText(movieDetails[MovieDBJsonUtils.SYNOPSIS_ITEM]);


            }
        }
    }
}
