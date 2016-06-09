package com.test.yanis.goeuro;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by Yanis on 09.06.2016.
 */
public class GoEuroApplication extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String API_URL = "http://api.goeuro.com/";
    private Retrofit mRetrofit;
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private GoEuroApi mService;

    private GoogleApiClient mGoogleApiClient = null;

    private BehaviorSubject<Location> mLocationSubject = BehaviorSubject.create();

    public void onCreate() {
        super.onCreate();

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        mRetrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(ResponseGsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        mService = mRetrofit.create(GoEuroApi.class);
    }

    public void initCoarseLocation(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public GoEuroApi getApi() {
        return mService;
    }

    public Subject<Location, Location> getLocationSubject() {
        return mLocationSubject;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLocationSubject.onNext(mLastLocation);
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
