package com.shriyansh.sunshine;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Arrays;
import java.util.List;


public class ForecastFragment extends Fragment {

    public ForecastFragment() {


    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(android.app.Activity)} and before
     * {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}.
     * <p/>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(android.os.Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String forecastList[]={
                "Today - Sunny  -  88 / 63",
                "Monday 16 - Clear  -  45 / 48",
                "Tuesday 17 - Rain  -  16 / 49",
                "Thursday 18- Sunny  -  88 / 63",
                "Friday 19 - Clear  -  45 / 48",
                "Saturday 20 - Rain  -  16 / 49",
                "Sunday 21 - Sunny  -  88 / 63",
                "Monday 22 - Clear  -  45 / 48",
                "Tuesday 23 - Rain  -  16 / 49",
                "Thursday 24 - Sunny  -  88 / 63",
                "Friday 25 - Clear  -  45 / 48",
                "Saturday 26 - Rain  -  16 / 49"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastList));

        ArrayAdapter<String> forecastAdapter= new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,forecastList);
        ListView listView=(ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        return rootView;
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment,menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();

        if(id==R.id.action_refresh){

            FetchWeatherTask weatherTask =new FetchWeatherTask();
            weatherTask.execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    public class FetchWeatherTask extends AsyncTask<String,Void,Void>{

       String LOG_TAG=ForecastFragment.class.getSimpleName();

       /**
        * Override this method to perform a computation on a background thread. The
        * specified parameters are the parameters passed to {@link #execute}
        * by the caller of this task.
        * <p/>
        * This method can call {@link #publishProgress} to publish updates
        * on the UI thread.
        *
        * @param params The parameters of the task.
        * @return A result, defined by the subclass of this task.
        * @see #onPreExecute()
        * @see #onPostExecute
        * @see #publishProgress
        */
       @Override
       protected Void doInBackground(String... params) {

           // These two need to be declared outside the try/catch
           // so that they can be closed in the finally block.

           if(params.length==0){
               return null;
           }

           HttpURLConnection urlConnection = null;
           BufferedReader reader = null;

           // Will contain the raw JSON response as a string.
           String forecastJsonStr = null;
           String format= "json";
           String units="metric";
           int numDays=7;

           try {
               // Construct the URL for the OpenWeatherMap query
               // Possible parameters are avaiable at OWM's forecast API page, at
               // http://openweathermap.org/API#forecast
               final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
               final String QUERY_PARAM="q";
               final String FORMAT_PARAM="mode";
               final String UNITS_PARAM="units";
               final String DAYS_PARAM="cnt";

               Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                       .appendQueryParameter(QUERY_PARAM,params[0])
                       .appendQueryParameter(FORMAT_PARAM,format)
                       .appendQueryParameter(UNITS_PARAM,units)
                       .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                       .build();

               URL url = new URL(builtUri.toString());

               Log.d(LOG_TAG,"Built Uri "+builtUri.toString());
               // Create the request to OpenWeatherMap, and open the connection
               urlConnection = (HttpURLConnection) url.openConnection();
               urlConnection.setRequestMethod("GET");
               urlConnection.connect();

               // Read the input stream into a String
               InputStream inputStream = urlConnection.getInputStream();
               StringBuffer buffer = new StringBuffer();
               if (inputStream == null) {
                   // Nothing to do.
                   return null;
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
                   // Stream was empty. No point in parsing.
                   return null;
               }
               forecastJsonStr = buffer.toString();

               //Logging the response
               Log.d(LOG_TAG,forecastJsonStr);

           } catch (IOException e) {
               Log.e(LOG_TAG, "Error ", e);
               // If the code didn't successfully get the weather data, there's no point in attemping
               // to parse it.
               return null;
           } finally{
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


           return null;
       }
   }

}
