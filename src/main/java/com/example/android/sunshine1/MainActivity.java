package com.example.android.sunshine1;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    private static String LOG_TAG = MainActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
        Log.v(LOG_TAG, "This is from onCreate() method");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //Anahat - Calling Explicit Intent Settings when the user selects settings from Menu
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        //Anahat - Calling Implicit Intent Android Maps when the user selects Maps from the menu
        if(id == R.id.action_map){
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }

    //Anahat - Method created to call Maps intent
    private void openPreferredLocationInMap(){
        //Anahat - Declaring constant string for building the Maps intent URI
        final String MAPS_INTENT_BASE_STRING = "geo:0,0";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String locationValue = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_location_key),getResources().getString(R.string.pref_location_default));
        Uri mapsIntentUri = Uri.parse(MAPS_INTENT_BASE_STRING).buildUpon().appendQueryParameter("q",locationValue).build();
        Log.v(this.getClass().toString(),"Maps intent URI: "+mapsIntentUri.toString());
        intent.setData(mapsIntentUri);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
        else{
            Log.d(this.getClass().toString(),"No Maps app found on the device");
        }
    }

    //Anahat - Below is the experiment on application lifecycle. Added debug statements on the activity lifecycle methods to understand the sequence of their calls.
    @Override
    public void onStart(){
        super.onStart();
        Log.v(LOG_TAG, "This is from onStart() method");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v(LOG_TAG, "This is from onStop() method");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG, "This is from onPause() method");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(LOG_TAG, "This is from onResume() method");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG, "This is from onDestroy() method");
    }
}
