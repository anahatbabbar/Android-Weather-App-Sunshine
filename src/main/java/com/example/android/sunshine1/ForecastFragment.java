package com.example.android.sunshine1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by anahat.babbar on 7/31/2014.
 */
public class ForecastFragment extends Fragment {

    //Anahat - Bringing the adapter outside the scope of onCreateView so that it can be accessed and updated by onPostExecute method in AsyncTask
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    //Anahat - onCreate method is called when the fragment is created. onCreateView method is called when the fragment UI is initialized, i.e. after onCreate().

    //Anahat - This method onCreate is added in Fragment code to activate Menus in fragment. setHasOptionsMenu is added so that menu related call-back methods are called on any events on menu
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Anahat - to set that the fragment has a options menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Anahat - Commenting the code below which was added to test the app in the very beginning of building this app

//        //Array List of items to be displayed in the ListView
//        String[] forecastArray = {
//                "Today - Sunday - 88/63",
//                "Tomorrow - Foggy - 70/45",
//                "Weds - Cloudy - 72/63",
//                "Thurs - Rainy - 64/51",
//                "Fri - Foggy - 70/63",
//                "Sat - Sunny - 76/68"
//        };
//
//
//        List<String> weekForecast = new ArrayList(Arrays.asList(forecastArray));

        // Anahat Commenting stopped for the above code

        //Get the adapter to bind data to the element in the layout xml file
        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast_1, R.id.list_item_forecast_textview_1, new ArrayList<String>()/*weekForecast*/);

        //now bind the adapter to the view. This is because nowhere we have defined how this data is associated with the view
        //First find the view in the hierarchy of layout i.e. in fragment_main xml
        ListView listContainer = (ListView) rootView.findViewById(R.id.listview_forecast);
        //now bind the data to the view
        listContainer.setAdapter(mForecastAdapter);

        // Anahat - Code below to take weather string from one activity and passing it on to another activity.
        listContainer.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            //Anahat - the parameters in the below methods are variables ListView listContainer, TextView, position of item in adapter , and row number ????
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String weatherString = (String)adapterView.getItemAtPosition(i);
                // Anahat - Toast used below to test passing of weather string
                /* Commenting the TOAST for now as it was only for debugging
                Toast toast = Toast.makeText(getActivity(),weatherString, Toast.LENGTH_SHORT);
                toast.show();
                */

                //Anahat - Calling explicit intent to call DetailActivity
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,weatherString );
                //Anahat - StartActivity method below calls the onCreate method of DetailActivity.
                startActivity(intent);
            }
        });
        return rootView;
    }


    //Anahat - The menu method below is a little different from activity menu button. Here the Menu inflater is passed as argument to the method.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    //Anahat - Method added to handle menu events.
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {

           //Anahat - Calling custom update method to fetch fresh data from the server
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Anahat - Method added so that it calls Aync task to fetch data from the server
    private void updateWeather(){
        //Anahat - getting the location details from SharedPreference
        String locationValue = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(getResources().getString(R.string.pref_location_key),"");

        //Anahat - Calling Async Task
        new FetchWeatherTask().execute(locationValue);
    }

    //Anahat - adding onStart method that is called the moment the app is launched
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        //Anahat - Updating the adapter with the result received from doInBAckground method
        @Override
        protected void onPostExecute(String[] result) {
            if(result != null){
                mForecastAdapter.clear();
                for(String dayForecastStr : result){
                    mForecastAdapter.add(dayForecastStr);
                }
            }

        }

        @Override
        public String[] doInBackground(String... params){
            //Adding code to connect ot a HTTP server

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int days = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //Anahat - Below is the URL passed without URL builder. A constant string is just paased. But it is non-dynamic
//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&cnt=7&units=metric&mode=json");

               //Anahat - Below is the recommended method of creating the URL using the Uri.Builder class.
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM,params[0])
                                .appendQueryParameter(FORMAT_PARAM,format)
                                .appendQueryParameter(UNITS_PARAM,units)
                                .appendQueryParameter(DAYS_PARAM,Integer.toString(days)).build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI "+ url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                //Log statement verbose is added as a debug string
//                Log.v(LOG_TAG, "Forecast JSON string is: "+forecastJsonStr);
                try {
                    String[] forecastArray = getWeatherDataFromJson(forecastJsonStr, days);
                    return forecastArray;
                }
                catch(JSONException e){
                    Log.e(LOG_TAG, "Error", e);
                    e.printStackTrace();
                    return null;
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
//            return null;
            //end
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.

           //Anahat - checking for the sharedPreference of units chosen to display the values by the user
            String tempUnit = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.temp_unit_key),"");
            if(tempUnit.equals("imperial")) {
                high = metricToImperial(high);
                low = metricToImperial(low);
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private double metricToImperial(double temp){
            temp = ((temp*9)/5) + 32;
            return temp;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }
    }
}