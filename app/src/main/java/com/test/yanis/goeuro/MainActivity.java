package com.test.yanis.goeuro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;

    private GoEuroApplication mApplication;
    private Subscription mSubscription;
    private Subscription mSubscription2;

    private MyAutoAdapter mFromAdapter;
    private MyAutoAdapter mToAdapter;
    private AutoCompleteTextView from;
    private AutoCompleteTextView to;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplication = (GoEuroApplication)getApplication();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        } else {
            mApplication.initCoarseLocation();
        }

        mFromAdapter = new MyAutoAdapter(mApplication.getApi());
        mToAdapter = new MyAutoAdapter(mApplication.getApi());

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        search = (Button) findViewById(R.id.search);

        from.setAdapter(mFromAdapter);
        to.setAdapter(mToAdapter);
        to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    to.dismissDropDown();
                    Utils.hideSoftKeyboard2(to);
                }
                return false;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Search is not yet implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            for(int i = 0; i < permissions.length; i++){
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    mApplication.initCoarseLocation();
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        mSubscription = mApplication.getLocationSubject().subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                mFromAdapter.setCurrentLocation(location);
                mToAdapter.setCurrentLocation(location);
            }
        });

        mSubscription2 = Observable.combineLatest(
                RxTextView.textChangeEvents(from),
                RxTextView.textChangeEvents(to), new Func2<TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
                    @Override
                    public Boolean call(TextViewTextChangeEvent from, TextViewTextChangeEvent to) {
                        return from.text().length() > 0 && to.text().length() > 0;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean enable) {
                        search.setEnabled(enable);
                    }
                });
    }

    @Override
    public void onPause(){
        if (mSubscription != null){
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        if (mSubscription2 != null){
            mSubscription2.unsubscribe();
            mSubscription2 = null;
        }
        super.onPause();
    }
}
