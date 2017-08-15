package com.example.samarthkejriwal.easyroads;

import com.example.samarthkejriwal.easyroads.Response.DistanceResponse;
import com.example.samarthkejriwal.easyroads.Response.PlacesResponse;
import com.example.samarthkejriwal.easyroads.Response.Places_details;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by samarthkejriwal on 10/08/17.
 */

public interface APIInterface {

    @GET("place/nearbysearch/json?")
    Call<PlacesResponse.Root> doPlaces(@Query(value = "location", encoded = true) String location, @Query(value = "radius", encoded = true) long radius, @Query(value = "type", encoded = true) String type, @Query(value = "key", encoded = true) String key);


    @GET("distancematrix/json?") // origins/destinations:  LatLng as string
    Call<DistanceResponse> getDistance(@Query(value ="origins", encoded = true) String origins, @Query(value = "destinations", encoded = true) String destinations, @Query(value = "key", encoded = true) String  key );

    @GET("place/details/json?")
    Call<Places_details> getPlaceDetails(@Query(value = "placeid", encoded = true) String placeid , @Query(value = "key", encoded = true) String key);

    @GET("geocode/json?")
    Call<Places_details> getCurrentAddress(@Query(value = "latlng", encoded = true) String latlng ,@Query(value = "key", encoded = true) String key);


}
