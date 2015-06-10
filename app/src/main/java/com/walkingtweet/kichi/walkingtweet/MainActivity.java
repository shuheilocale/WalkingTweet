package com.walkingtweet.kichi.walkingtweet;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    AccelerometerAdapter ad;
    GraphCounter thread;
    private AsyncTwitter mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ad = new AccelerometerAdapter(manager);

        thread = new GraphCounter(ad);
        thread.start();

        mTwitter = new AsyncTwitterFactory().getInstance();
        mTwitter.addListener(mListener);

        mTwitter.setOAuthConsumer(APIKey, APISecret);

        AccessToken token = getAccessToken();
        if (token == null) {
            mTwitter.getOAuthRequestTokenAsync("twittercallback://callback");

        }
        else {
            mTwitter.setOAuthAccessToken(token);
        }

        Button btnTweet = (Button)findViewById(R.id.tweetbutton);


        btnTweet.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
                String	tweetString;
                tweetString = String.format("今日は %d歩 歩きました。", ad.getStepCounter());

                mTwitter.updateStatus(tweetString);

                Toast.makeText(getApplicationContext(),
                        "ツイートしました",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ad.stopSensor();
        thread.close();
    }

    // ロギングスレッド
    class GraphCounter extends Thread {
        AccelerometerAdapter ad;
        Handler handler = new Handler();

        TextView tvx;
        TextView tvy;
        TextView tvz;
        TextView stepcount;
        TextView perminstepcount;
        GraphView grx;
        GraphView gry;
        GraphView grz;
        boolean runflg = true;
        long curTime = System.currentTimeMillis();
        long curStep = 0;

        public GraphCounter(AccelerometerAdapter ad) {
            this.ad = ad;
            tvx = (TextView) findViewById(R.id.Xdata);
            tvy = (TextView) findViewById(R.id.Ydata);
            tvz = (TextView) findViewById(R.id.Zdata);
            grx = (GraphView) findViewById(R.id.graphViewX);
            gry = (GraphView) findViewById(R.id.graphViewY);
            grz = (GraphView) findViewById(R.id.graphViewZ);
            stepcount = (TextView) findViewById(R.id.stepcnt);
            perminstepcount = (TextView) findViewById(R.id.perminstepcnt);
        }

        public void close() {
            runflg = false;
        }

        public void run() {
            while (runflg) {
                long t = System.currentTimeMillis() - curTime;
                if( t >= 60000 ) {
                    curTime = System.currentTimeMillis();
                    curStep = ad.getStepCounter();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvx.setText("" + ad.getDx());
                        tvy.setText("" + ad.getDy());
                        tvz.setText("" + ad.getDz());
                        grx.setDiv(ad.getDx());
                        gry.setDiv(ad.getDy());
                        grz.setDiv(ad.getDz());
                        grx.invalidate();
                        gry.invalidate();
                        grz.invalidate();

                        stepcount.setText("" + ad.getStepCounter());
                        long perminstep = ad.getStepCounter() - curStep;
                        perminstepcount.setText( "" + perminstep );
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final String APIKey =

    				"****************";
    private static final String APISecret =

				"****************";

    private final String PREF_FILE_NAME = "YMOTweetTest";
    private final String PREF_TOKEN = "token";
    private final String PREF_SECRET = "secret";


    private RequestToken mReqToken;
    //		private EditText eText;
//		private Button btnTweet;
    private final TwitterListener mListener = new TwitterListener();


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.e(TAG, "intent-------->" + intent.getDataString());

        //ブラウザからのコールバック
        final Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("twittercallback://callback")) {
            final String verifier = uri.getQueryParameter("oauth_verifier");
            try {/*
                AccessToken at = mOauth.getOAuthAccessToken(verifier);
                mAccessToken = at.getToken();
                mAccessTokenSecret = at.getTokenSecret();
                createTwitterInstance();*/
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            if (verifier != null) {
                mTwitter.getOAuthAccessTokenAsync(mReqToken, verifier);
            }
        }

    }

    protected class TwitterListener extends TwitterAdapter {
        @Override
        public void gotOAuthRequestToken(RequestToken token) {
            mReqToken = token;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReqToken.getAuthorizationURL()));
            startActivity(intent);
        }

        @Override
        public void gotOAuthAccessToken(AccessToken token) {
            SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            Editor editor = pref.edit();

            editor.putString(PREF_TOKEN, token.getToken());
            editor.putString(PREF_SECRET, token.getTokenSecret());
            editor.commit();

            mTwitter.setOAuthAccessToken(new AccessToken(token.getToken(), token.getTokenSecret()));
        }
    }


    public AccessToken getAccessToken() {

        SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        String token = pref.getString(PREF_TOKEN, null);
        String secret = pref.getString(PREF_SECRET, null);


        if (token != null && secret != null) {
            return new AccessToken(token, secret);
        } else {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
