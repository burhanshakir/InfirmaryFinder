package gcm.burhan.android.infirmaryfinder.service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import gcm.burhan.android.infirmaryfinder.R;


/**
 * Created by burha on 23-02-2017.
 */

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "Hello World";

    public LocationManager locationManager;
    public MyLocationListener listener;
    double currentLong, currentLat;

    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }



    @Override
    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0,listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates( listener);
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.i("Update Location", "Location changed");
            currentLat = loc.getLatitude();
            currentLong = loc.getLongitude();
            intent.putExtra("Latitude", loc.getLatitude());
            intent.putExtra("Longitude", loc.getLongitude());
            intent.putExtra("Provider", loc.getProvider());
            sendBroadcast(intent);

            Log.d("Location", String.valueOf(loc.getLatitude()));

            BackgroundTask backgroundTask = new BackgroundTask();
            backgroundTask.execute(String.valueOf(sbMethod()));

        }

        StringBuilder sbMethod() {

            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=").append(currentLat).append(",").append(currentLong);
            sb.append("&radius=500");
            sb.append("&types=" + "hospital");
            sb.append("&sensor=true");
            sb.append("&key=AIzaSyA6Oi-2LGaXQWA2c9VHS0lqcCe7So7ku2g");

            Log.d("Map", "api: " + sb.toString());

            return sb;
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        private class BackgroundTask extends AsyncTask<String, String, String> {

            String data = null;

            // Invoked by execute() method of this object
            @Override
            protected String doInBackground(String... url) {
                try {
                    data = downloadUrl(url[0]);
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                JSONObject jObject;

                try {
                    jObject = new JSONObject(result);

                    String status = jObject.getString("status");
                    Log.d("Result from service",status);

                    if(!status.equalsIgnoreCase("ZERO_RESULTS")){

                        if(jObject.getJSONArray("results").length()>0) {

                            Log.d("Length from service", String.valueOf(jObject.getJSONArray("results").length()));
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(LocationService.this)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle(getResources().getString(R.string.app_name))
                                            .setContentText(getResources().getString(R.string.hurray));

                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(123, mBuilder.build());

                            stopSelf();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            HttpURLConnection urlConnection;
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            // Reading data from url
            try (InputStream iStream = urlConnection.getInputStream()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Exception while downloading url", e.toString());
            } finally {
                urlConnection.disconnect();
            }
            return data;
        }

    }
}

