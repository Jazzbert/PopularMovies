package com.craigcleveland.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.craigcleveland.popularmovies.data.MovieContract;

import us.feras.mdv.MarkdownView;

public class ReviewActivity extends AppCompatActivity {

    private TextView mMovieTitleTextView;
    private TextView mReviewAuthorTextView;
    private MarkdownView mReviewContentMarkdownView;

    private static String sMovieTitle;
    private static String sReviewAuthor;
    private static String sReviewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        sMovieTitle = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_TITLE);
        sReviewAuthor = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR);
        sReviewContent = getIntent().getStringExtra(MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT);

        mMovieTitleTextView = (TextView) findViewById(R.id.tv_review_title);
        mReviewAuthorTextView = (TextView) findViewById(R.id.tv_review_author);
        mReviewContentMarkdownView = (MarkdownView) findViewById(R.id.md_review_content);

        mMovieTitleTextView.setText(sMovieTitle);
        mReviewAuthorTextView.setText("Review by " + sReviewAuthor);
        mReviewContentMarkdownView.loadMarkdown(sReviewContent);

    }
}
