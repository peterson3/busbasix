package com.example.tiago.busbasix;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.facebook.FacebookActivity;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent;
                ProfileTracker fbProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        // User logged in or changed profile
                    }
                };

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                Profile profile = Profile.getCurrentProfile();

                if (profile != null) {
                   homeIntent = new Intent(MainActivity.this, MapsActivity.class);
                    //homeIntent = new Intent(MainActivity.this, AuthActivity.class);
                    Log.i("MyApp", profile.getFirstName());
                }
                else{
                    homeIntent = new Intent(MainActivity.this, AuthActivity.class);
                }

                startActivity(homeIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
