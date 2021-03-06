package com.github.mecharyry.auth.oauth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import com.github.mecharyry.R;

public class OAuthWebViewActivity extends Activity {

    public static final int REQUEST_CODE = 100;
    public static final String EXTRA_REQUEST_URL = "URL";
    public static final String OAUTH_VERIFIER = "oauth_verifier";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_requester);
        WebView webView = (WebView) findViewById(R.id.webviewer);

        if (getIntent().hasExtra(EXTRA_REQUEST_URL)) {
            String url = getIntent().getStringExtra(EXTRA_REQUEST_URL);
            webView.loadUrl(url);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri responseUri = intent.getData();
        if (responseUri != null) {
            String oauthVerifier = responseUri.getQueryParameter(OAUTH_VERIFIER);
            Intent intentVerifier = new Intent();
            intentVerifier.putExtra(OAUTH_VERIFIER, oauthVerifier);
            setResult(Activity.RESULT_OK, intentVerifier);
            finish();
        }
    }
}
