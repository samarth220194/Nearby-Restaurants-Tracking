package com.example.samarthkejriwal.easyroads;

import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samarthkejriwal.easyroads.Modal.PlacesDetails_Modal;
import com.example.samarthkejriwal.easyroads.Response.DistanceResponse;
import com.example.samarthkejriwal.easyroads.Response.PlacesResponse;
import com.example.samarthkejriwal.easyroads.Response.PlacesResponse.CustomA;
import com.example.samarthkejriwal.easyroads.Response.PlacesResponse.Root;
import com.example.samarthkejriwal.easyroads.Response.Places_details;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    APIInterface apiService;
    public String latLngString;
    public double source_lat,source_long;

    RecyclerView recyclerView;
    public static final String  PREFS_FILE_NAME = "sharedPreferences";
    ArrayList<PlacesResponse.CustomA> results;

    protected Location mLastLocation;

    ProgressBar progress;

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;

    public ArrayList<PlacesDetails_Modal> details_modal;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    private long radius = 3 * 1000 ;

    private static final int MY_PERMISION_CODE = 10;

    private boolean Permission_is_granted = false;
    public String mAddressOutput;
    public RelativeLayout rl_layout;
    public String Location_type = "ROOFTOP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("On Create","true");

        progress = (ProgressBar) findViewById(R.id.progress);
         rl_layout  = (RelativeLayout) findViewById(R.id.rl_layout);

        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            progress.setProgress(0,true);
        }
        else progress.setProgress(0);



        apiService = ApiClient.getClient().create(APIInterface.class);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Manual check internet conn. on activity start
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            showSnack(true);
        } else {
            progress.setVisibility(View.GONE);
            showSnack(false);
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_refresh:

                progress.setVisibility(View.VISIBLE);
                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    progress.setProgress(0,true);
                }
                else progress.setProgress(0);

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    showSnack(true);
                } else {
                    progress.setVisibility(View.GONE);
                    showSnack(false);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fetchStores(String placeType) {

        Call<PlacesResponse.Root> call = apiService.doPlaces(latLngString,radius,placeType, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<Root>() {
            @Override
            public void onResponse(Call<PlacesResponse.Root> call, Response<PlacesResponse.Root> response) {
                PlacesResponse.Root root = (Root) response.body();


                if (response.isSuccessful()) {

                    switch (root.status) {
                        case "OK":

                            results = root.customA;

                            details_modal = new ArrayList<PlacesDetails_Modal>();
                            String photourl;
                            Log.i(TAG, "fetch stores");


                            for (int i = 0; i < results.size(); i++) {

                                CustomA info = (CustomA) results.get(i);

                                String place_id = results.get(i).place_id;


                                if (results.get(i).photos != null) {

                                    String photo_reference = results.get(i).photos.get(0).photo_reference;

                                     photourl = ApiClient.base_url + "place/photo?maxwidth=100&photoreference=" + photo_reference +
                                            "&key=" + ApiClient.GOOGLE_PLACE_API_KEY;

                                } else {
                                   photourl = "NA";
                                }

                                fetchDistance(info,place_id,photourl);


                                Log.i("Coordinates  ", info.geometry.locationA.lat + " , " + info.geometry.locationA.lng);
                                Log.i("Names  ",info.name);

                            }

                            break;
                        case "ZERO_RESULTS":
                            Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                            break;
                        case "OVER_QUERY_LIMIT":
                            Toast.makeText(getApplicationContext(), "You have reached the Daily Quota of Requests", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error in Fetching Details,Please Refresh",Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }


    private void fetchDistance(final PlacesResponse.CustomA info, final String place_id, final String photourl) {

        Log.i(TAG,"Distance API call start");

        Call<DistanceResponse> call = apiService.getDistance(latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng,ApiClient.GOOGLE_PLACE_API_KEY);

        call.enqueue(new Callback<DistanceResponse>() {
            @Override
            public void onResponse(Call<DistanceResponse> call, Response<DistanceResponse> response) {

                DistanceResponse resultDistance = (DistanceResponse) response.body();

                if (response.isSuccessful()) {

                    Log.i(TAG, resultDistance.status);

                    if ("OK".equalsIgnoreCase(resultDistance.status)) {
                        DistanceResponse.InfoDistance row1 = resultDistance.rows.get(0);
                        DistanceResponse.InfoDistance.DistanceElement element1 = row1.elements.get(0);

                        if ("OK".equalsIgnoreCase(element1.status)) {

                            DistanceResponse.InfoDistance.ValueItem itemDistance = element1.distance;

                            String total_distance = itemDistance.text;

                            fetchPlace_details(info, place_id, total_distance, info.name, photourl);
                        }


                    }

                }
                else if (response.code() != 200){
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error in Fetching Details,Please Refresh",Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }


    private void fetchPlace_details(final PlacesResponse.CustomA info, final String place_id, final String totaldistance, final String name, final String photourl)
    {

            Call<Places_details> call = apiService.getPlaceDetails(place_id, ApiClient.GOOGLE_PLACE_API_KEY);
            call.enqueue(new Callback<Places_details>() {
                @Override
                public void onResponse(Call<Places_details> call, Response<Places_details> response) {

                    Places_details details = (Places_details) response.body();

                    if ("OK".equalsIgnoreCase(details.status)) {

                        String address = details.result.formatted_adress;
                        String phone = details.result.international_phone_number;
                        float rating = details.result.rating;

                        details_modal.add(new PlacesDetails_Modal(address, phone, rating,totaldistance,name,photourl));

                        Log.i("details : ", info.name + "  " + address);

                        if (details_modal.size() == results.size()) {

                            Collections.sort(details_modal, new Comparator<PlacesDetails_Modal>() {
                                @Override
                                public int compare(PlacesDetails_Modal lhs, PlacesDetails_Modal rhs) {
                                    return  lhs.distance.compareTo(rhs.distance);
                                }
                            });

                            progress.setVisibility(View.GONE);
                            Rv_adapter adapterStores = new Rv_adapter(getApplicationContext(), details_modal, mAddressOutput);
                            recyclerView.setAdapter(adapterStores);
                            adapterStores.notifyDataSetChanged();
                        }

                    }

                }

                @Override
                public void onFailure(Call call, Throwable t)
                {
                    call.cancel();
                }
            });

    }


    private void fetchCurrentAddress(final String latLngString) {

        Call<Places_details> call = apiService.getCurrentAddress(latLngString,ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<Places_details>() {
            @Override
            public void onResponse(Call<Places_details> call, Response<Places_details> response) {

                Places_details details = (Places_details) response.body();

                if ("OK".equalsIgnoreCase(details.status)) {

                    mAddressOutput = details.results.get(0).formatted_adress;
                    Log.i("Addr Current and coord.",mAddressOutput + latLngString);
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                call.cancel();
            }
        });

    }





    private void getUserLocation()
    {

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (VERSION.SDK_INT >= VERSION_CODES.M)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                       ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                        ACCESS_COARSE_LOCATION) )
                {
                    showAlert();

                }
                else
                    {

                    if(isFirstTimeAskingPermission(this, permission.ACCESS_FINE_LOCATION))
                    {
                        firstTimeAskingPermission(this,
                                permission.ACCESS_FINE_LOCATION, false);
                        ActivityCompat.requestPermissions(this,
                                new String[]{ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);
                    } else {

                        Toast.makeText(this,"You won't be able to access the features of this App",Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                        //Permission disable by device policy or user denied permanently. Show proper error message
                    }



                }


            }
            else Permission_is_granted = true;
        }
        else
        {

            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if (location != null) {

                        mLastLocation = location;
                        source_lat = location.getLatitude();
                        source_long = location.getLongitude();
                        latLngString = location.getLatitude() + "," + location.getLongitude();
                        fetchCurrentAddress(latLngString);

                        Log.i(TAG, latLngString + "");

                        fetchStores("restaurant");

                    }
                }
            });
        }
    }




    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    public static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)

    {
        Log.i("On request permiss","executed");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case MY_PERMISION_CODE:

                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Permission_is_granted = true;
                    getUserLocation();
                }
            else
                {
                    showAlert();
                    Permission_is_granted = false;
                    Toast.makeText(getApplicationContext(),"Please switch on GPS to access the features",Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.GONE);

            }
                break;

        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("google api client","coonected");
        if(Permission_is_granted) {
            getUserLocation();
        }
        //  requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        Log.i("on start","true");
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {

        Log.i("on resume","true");
        super.onResume();
        if(Permission_is_granted) {
            if (mGoogleApiClient.isConnected()) {
                //  getUserLocation();
            }
        }
    }

    @Override
    protected void onPause() {
        Log.i("on pause","true");

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings are OFF \nPlease Enable Location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    public void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
            getUserLocation();
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(rl_layout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }




}


