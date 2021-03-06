package com.github.mecharyry.tweetlist.task;

import android.content.ContentValues;

import com.github.mecharyry.auth.oauth.AccessToken;
import com.github.mecharyry.auth.oauth.OAuthAuthenticator;
import com.github.mecharyry.tweetlist.parser.ParserFactory;
import com.github.mecharyry.tweetlist.request.RequestFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class TaskFactory {
    public static final String ANDROID_DEV_TWEETS = "https://api.twitter.com/1.1/search/tweets.json?q=%23AndroidDev&count=30";
    public static final String MY_STREAM_TWEETS = "https://api.twitter.com/1.1/statuses/home_timeline.json?count=30";
    public static final String ERROR_SIGNING_URL_MESSAGE = "While signing URL.";
    private final OAuthConsumer consumer;
    private final ParserFactory parserFactory;
    private final RequestFactory requestFactory;


    public static TaskFactory newInstance(AccessToken accessToken) {
        OAuthAuthenticator oAuthAuthenticator = OAuthAuthenticator.newInstance();
        OAuthConsumer oAuthConsumer = oAuthAuthenticator.getConsumer(accessToken);
        ParserFactory parserFactory = ParserFactory.newInstance();
        RequestFactory requestFactory = new RequestFactory();
        return new TaskFactory(oAuthConsumer, parserFactory, requestFactory);
    }

    TaskFactory(OAuthConsumer consumer, ParserFactory parserFactory, RequestFactory requestFactory) {
        this.consumer = consumer;
        this.parserFactory = parserFactory;
        this.requestFactory = requestFactory;
    }

    public Task<JSONObject, ContentValues[]> requestAndroidDevTweets() {
        String signedUrl = signUrl(ANDROID_DEV_TWEETS);
        return new Task<JSONObject, ContentValues[]>(parserFactory.hashtagParser(), requestFactory.twitterObjectRequest(), signedUrl);
    }

    public Task<JSONObject, ContentValues[]> requestAndroidDevTweetsBeforeId(long id) {
        String signedUrl = signUrl(ANDROID_DEV_TWEETS + "&max_id=" + id);
        return new Task<JSONObject, ContentValues[]>(parserFactory.hashtagParser(), requestFactory.twitterObjectRequest(), signedUrl);
    }

    public Task<JSONArray, ContentValues[]> requestMyStreamTweets() {
        String signedUrl = signUrl(MY_STREAM_TWEETS);
        return new Task<JSONArray, ContentValues[]>(parserFactory.myStreamParser(), requestFactory.twitterArrayRequest(), signedUrl);
    }

    public Task<JSONArray, ContentValues[]> requestMyStreamTweetsBeforeId(long id) {
        String signedUrl = signUrl(MY_STREAM_TWEETS + "&max_id=" + id);
        return new Task<JSONArray, ContentValues[]>(parserFactory.myStreamParser(), requestFactory.twitterArrayRequest(), signedUrl);
    }

    private String signUrl(String unsignedUrl) {
        String signedUrl = null;
        try {
            signedUrl = consumer.sign(unsignedUrl);
        } catch (OAuthMessageSignerException e) {
            throwAuthException(ERROR_SIGNING_URL_MESSAGE, e);
        } catch (OAuthExpectationFailedException e) {
            throwAuthException(ERROR_SIGNING_URL_MESSAGE, e);
        } catch (OAuthCommunicationException e) {
            throwAuthException(ERROR_SIGNING_URL_MESSAGE, e);
        }
        return signedUrl;
    }

    private void throwAuthException(String reason, Exception exception) {
        throw OAuthException.because(reason, exception);
    }

    public static class OAuthException extends RuntimeException {

        public static OAuthException because(String reason, Throwable throwable) {
            return new OAuthException(reason, throwable);
        }

        private OAuthException(String reason, Throwable throwable) {
            super(reason, throwable);
        }
    }
}
