package com.github.mecharyry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.github.mecharyry.db.task.RetrieveWritableDatabaseTask;
import com.github.mecharyry.tweetlist.adapter.mapping.Tweet;
import com.github.mecharyry.tweetlist.parser.Parser;
import com.github.mecharyry.tweetlist.parser.ParserFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String TAG = Database.class.getSimpleName();
    private final ExtendedSQLiteOpenHelper helper;
    private final Parser<byte[], Bitmap> byteArrayToBitmapParser;
    private final Parser<Bitmap, byte[]> bitmapToByteArrayParser;
    private SQLiteDatabase sqLiteDatabase;

    public static Database newInstance(Context context) {
        ExtendedSQLiteOpenHelper helper = new ExtendedSQLiteOpenHelper(context);
        ParserFactory parserFactory = ParserFactory.newInstance();
        Parser<byte[], Bitmap> byteArrayToBitmapParser = parserFactory.byteArrayToBitmapParser();
        Parser<Bitmap, byte[]> bitmapToByteArrayParser = parserFactory.bitmapToByteArrayParser();

        return new Database(helper, byteArrayToBitmapParser, bitmapToByteArrayParser);
    }

    Database(ExtendedSQLiteOpenHelper helper, Parser<byte[], Bitmap> byteArrayToBitmapParser, Parser<Bitmap, byte[]> bitmapToByteArrayParser) {
        this.helper = helper;
        this.byteArrayToBitmapParser = byteArrayToBitmapParser;
        this.bitmapToByteArrayParser = bitmapToByteArrayParser;
    }

    public void open() {
        sqLiteDatabase = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    public boolean insertTweet(Tweet tweet) {
        if (sqLiteDatabase.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(TweetTable.COLUMN_SCREEN_NAME, tweet.getScreenName());
            values.put(TweetTable.COLUMN_LOCATION, tweet.getLocation());
            values.put(TweetTable.COLUMN_TWEET_TEXT, tweet.getText());

            byte[] imageData = bitmapToByteArrayParser.parse(tweet.getThumbImage());
            values.put(TweetTable.COLUMN_THUMB_IMAGE, imageData);

            sqLiteDatabase.insert(TweetTable.TABLE_NAME, null, values);
            return true;
        }
        return false;
    }

    public List<Tweet> getAllTweets() {
        List<Tweet> tweets = new ArrayList<Tweet>();
        Cursor cursor = sqLiteDatabase.query(TweetTable.TABLE_NAME, TweetTable.ALL_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Tweet tweet = cursorToTweet(cursor);
            tweets.add(tweet);

            cursor.moveToNext();
        }
        return tweets;
    }

    private Tweet cursorToTweet(Cursor cursor) {
        int idColumnIndex = cursor.getColumnIndex(TweetTable.COLUMN_ID);
        int locationColumnIndex = cursor.getColumnIndex(TweetTable.COLUMN_LOCATION);
        int screenNameColumnIndex = cursor.getColumnIndex(TweetTable.COLUMN_SCREEN_NAME);
        int textColumnIndex = cursor.getColumnIndex(TweetTable.COLUMN_TWEET_TEXT);
        int bitmapColumnIndex = cursor.getColumnIndex(TweetTable.COLUMN_THUMB_IMAGE);

        int id = cursor.getInt(idColumnIndex);
        String location = cursor.getString(locationColumnIndex);
        String screenName = cursor.getString(screenNameColumnIndex);
        String text = cursor.getString(textColumnIndex);
        byte[] imageData = cursor.getBlob(bitmapColumnIndex);

        Bitmap thumbImage = byteArrayToBitmapParser.parse(imageData);

        Tweet tweet = new Tweet(screenName, location, text, thumbImage);
        tweet.setId(id);
        return tweet;
    }
}
