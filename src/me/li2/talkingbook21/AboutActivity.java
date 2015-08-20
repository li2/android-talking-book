package me.li2.talkingbook21;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AboutActivity extends FragmentActivity {
    private static final String TAG = "AboutActivity";
    private static final String ABOUT_URL = "http://li2.me/android/talkingbook21-release-note/";

    private Context mContext;
    private WebView mWebView = null;
    private ProgressDialog mSpinner = null;
    private boolean isRedirected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(this) != null) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.catcher_about);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }

        mContext = getBaseContext();
        if (!isWebReachable()) {
            showNetworkAlert();
            finish();
            return;
        }
        mWebView = (WebView) findViewById(R.id.catcher_about);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!isRedirected) {
                    // Do something you want when starts loading
                }
                isRedirected = false;
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                isRedirected = true;
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(mContext, "Http error!" + description, Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                finish();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isRedirected) {
                    // Do something you want when finished loading
                    hideProgressDialog();
                }
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl(ABOUT_URL);
        showProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // To handle the back button key press
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSpinner == null) {
                    mSpinner = new ProgressDialog(AboutActivity.this);
                    mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    mSpinner.setMessage("Loading...");
                    mSpinner.setCanceledOnTouchOutside(false);
                }
                mSpinner.show();
            }
        });

    }

    private void hideProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSpinner != null) {
                    mSpinner.dismiss();
                }
            }
        });
    }

    private boolean isWebReachable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNetworkAlert() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.catcher_network_unreachable),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (NavUtils.getParentActivityName(this) != null) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
