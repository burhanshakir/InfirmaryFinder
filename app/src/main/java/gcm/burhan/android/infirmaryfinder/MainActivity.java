package gcm.burhan.android.infirmaryfinder;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.prefs.NodeChangeEvent;

import gcm.burhan.android.infirmaryfinder.adapter.HospitalAdapter;
import gcm.burhan.android.infirmaryfinder.model.Hospital;
import gcm.burhan.android.infirmaryfinder.service.LocationService;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    TextView tv;
    LocationRequest mLocationRequest;
    Geocoder geocoder;
    int flag = 0;
    double currentLong, currentLat;
    List<Address> addresses;
    Location currentLoc, targetLoc;
    Button start;
    LinearLayoutManager linearLayoutManager;
    HospitalAdapter adapter;
    RecyclerView recyclerView;
    private boolean checkForHospitals = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv = (TextView) findViewById(R.id.tvCurrentAdd);
        start = (Button) findViewById(R.id.bStart);
        recyclerView = (RecyclerView) findViewById(R.id.my_hospitals_recycler_view);

        buildGoogleApiClient();
        createLocationRequest();

        currentLoc = new Location("");
        targetLoc = new Location("");
        geocoder = new Geocoder(this, Locale.getDefault());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkLocationPermission();

        start.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bStart:
                if (flag == 0) {
                    start.setText(R.string.stop);
                    flag++;

                    fetchHospitals();
                    startLocationUpdates();


                }
                else {
                    stopLocationUpdates();
                    flag = 0;
                    start.setText(R.string.start);
                }
        }

    }

    private void checkIfHospitalsAvailable() {

        if(checkForHospitals){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.hurray));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(123, mBuilder.build());

            stopLocationUpdates();
            flag = 0;
            start.setText(R.string.start);
        }


    }

    private void startLocationUpdates() {
        checkManifestPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d("Start Location","startLocation");

    }

    private void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void checkManifestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchHospitals() {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute(String.valueOf(sbMethod()));
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO:
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }

                    }

                } else {

                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_LONG).show();
                }

            }

        }
    }


    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if(flag == 1) {
            Log.d("Pause", "Pausing");
            Intent serviceIntent = new Intent(this, LocationService.class);
            startService(serviceIntent);
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Destroy","Destroy");
        stopService(new Intent(MainActivity.this, LocationService.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection Failed.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        checkManifestPermission();
        currentLoc = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (currentLoc != null) {

            currentLong = currentLoc.getLongitude();
            currentLat = currentLoc.getLatitude();

            try {
                addresses = geocoder.getFromLocation(currentLat, currentLong, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getLocality();
            tv.setText(address);


        }

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        currentLoc = location;
        Log.d("Start Location","RequestLocation");
        fetchHospitals();
        checkIfHospitalsAvailable();
    }

    public StringBuilder sbMethod() {

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=").append(currentLat).append(",").append(currentLong);
        sb.append("&radius=500");
        sb.append("&types=" + "hospital");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyA6Oi-2LGaXQWA2c9VHS0lqcCe7So7ku2g");

        Log.d("Map", "api: " + sb.toString());

        return sb;
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

            //Toast.makeText(MainActivity.this, "Finding Hospitals near you..", Toast.LENGTH_SHORT).show();
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
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

    private class ParserTask extends AsyncTask<String, Integer, List<Hospital>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<Hospital> doInBackground(String... jsonData) {

            List<Hospital> places = null;
            HospitalsJSON placeJson = new HospitalsJSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }
        @Override
        protected void onPostExecute(List<Hospital> list) {

            Log.d("Map", "list size: " + list.size());
//            for (int i = 0; i < list.size(); i++) {
//
//                Hospital hmPlace = list.get(i);
//
//
//                double lat = hmPlace.getLatitude();
//                double lng = hmPlace.getLongitude();
//                String name = hmPlace.getName();
//                String vicinity = hmPlace.getVicinity();
//
//
//                Log.d("Map", "place: " + name);
            if(list.size()>0) {
                linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                adapter = new HospitalAdapter(MainActivity.this, list);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
                checkForHospitals = true;
            }
            else {
                checkForHospitals = false;
                //stopLocationUpdates();
            }


        }
    }
}
