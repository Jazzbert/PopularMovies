package com.craigcleveland.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.craigcleveland.popularmovies.data.MovieContract;

import us.feras.mdv.MarkdownView;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        String movieTitle;
        String reviewAuthor;
        String reviewContent;

        TextView movieTitleTextView;
        TextView reviewAuthorTextView;
        MarkdownView reviewContentMarkdownView;

        movieTitle = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_TITLE);
        reviewAuthor = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR);
        reviewContent = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT);

        movieTitleTextView = findViewById(R.id.tv_review_title);
        reviewAuthorTextView = findViewById(R.id.tv_review_author);
        reviewContentMarkdownView = findViewById(R.id.md_review_content);

        movieTitleTextView.setText(movieTitle);
        String revBy = getString(R.string.review_by) + reviewAuthor;
        reviewAuthorTextView.setText(revBy);
        reviewContentMarkdownView.loadMarkdown(reviewContent);

    }
}
