package com.example.android.sunshine1;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.os.Build;
//import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.android.sunshine1.R;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private String forecastStr;
        private static String LOG_TAG = PlaceholderFragment.class.getName();

        public PlaceholderFragment() {
            //Anahat - Set so that menu callback methods can be called
            //setHasOptionsMenu(true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            //Anahat - to set that the fragment has a options menu
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            //Anahat - Getting the weather string from Main Activity using the Intent passed to the activity.
            Intent intent = getActivity().getIntent();
            //Anahat - Search for the Bundle corresponding to EXTRA_TEXT added by calling activity
            forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            //Anahat - find the text view to set value to
            TextView textView = (TextView)rootView.findViewById(R.id.detail_text);
            textView.setText(forecastStr);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            //Anahat - Adding ShareActionProvider to detail fragment
            MenuItem item = menu.findItem(R.id.action_detail_share);

            //Anahat - Very important. The line below from the document did not work for me and threw java.lang.ClassCastException.This is because SharedActionProvider is in 2 separate packages
            //mShareActionProvider = (ShareActionProvider)item.getActionProvider();

            ShareActionProvider mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);

            if(mShareActionProvider != null){
                mShareActionProvider.setShareIntent(createIntentForShareAction());
            }
            else{
                Log.d(LOG_TAG, "Shared Action Provider is null");
            }
        }

        //Anahat - Method added to create ShareAction intent.
        private Intent createIntentForShareAction() {
                //Anahat - Creating forecast string in the intent and setting it in ShareActionProvider and calling it
                String shareStr = forecastStr + " #SunshineApp";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                //The line below is important. It tells the OS not to place the sharing app on the activity stack. Else, if we restart the app, we will be in the sharing app instead of sunshine app
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareStr);
                return shareIntent;
        }
    }
}
