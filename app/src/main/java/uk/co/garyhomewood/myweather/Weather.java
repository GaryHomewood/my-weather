package uk.co.garyhomewood.myweather;


import android.app.DownloadManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by garhom on 13/11/2015.
 */
public class Weather {

    private static final String TAG = "Weather";

    String mTemp;
    String mOverview;
    String mDescription;
    MapsActivity mMapsActivity;
    OkHttpClient client = new OkHttpClient();

    public String getTemp() {
        return mTemp;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getDescription() {
        return mDescription;
    }

    public Weather(MapsActivity context, Location location) {
        mMapsActivity = context;

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Address bestMatch = null;

        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> matches = geocoder.getFromLocation(lat, lon, 1);
            bestMatch = (matches.isEmpty() ? null : matches.get(0));
        } catch (IOException e) {
        }

        if (bestMatch != null) {
            try {
                callApi(bestMatch.getPostalCode());
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
       }
    }

    private void callApi(String postcode) throws Exception {

        String yql = "select%20*%20from%20weather.forecast%20"
                + "where%20woeid%20in%20"
                + "(select%20woeid%20from%20geo.places(1)%20"
                + "where%20text%3D%22" + postcode + "%22)";

        String url = "https://query.yahooapis.com/v1/public/yql?q="
                + yql
                + "&format=json&diagnostics=true&callback=";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonResponse = response.body().string();
                try {
                    JSONObject json = new JSONObject(jsonResponse);
                    JSONObject item = json.getJSONObject("query")
                                                    .getJSONObject("results")
                                                    .getJSONObject("channel")
                                                    .getJSONObject("item");

                    mTemp = item.getJSONObject("condition").getString("temp");
                    mOverview = item.getJSONObject("condition").getString("text");
                    mDescription = item.getString("description");

                    mMapsActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMapsActivity.updateUI();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
