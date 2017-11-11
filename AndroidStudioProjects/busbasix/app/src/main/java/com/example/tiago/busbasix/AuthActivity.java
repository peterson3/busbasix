package com.example.tiago.busbasix;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class AuthActivity extends AppCompatActivity {

    //Intent Actions
    private static final String MAPS_ACTIVITY = "com.example.tiago.busbasix.MapsActivity";

    LoginButton loginBtn;
    CallbackManager callbackManager;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_auth);
        //statusText = (TextView)findViewById(R.id.status_text);
        loginBtn = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("onSuccess",  "onSuccess callback");
                System.out.println( "Sucess on FACEBOOK login");
                Toast.makeText(getApplicationContext(), "Login com Facebook realizado com Sucesso!", Toast.LENGTH_SHORT).show();

                //GoTo MainPage
               //loginBtn.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(AuthActivity.this, MapsActivity.class);

                //Intent intent = new Intent(MAPS_ACTIVITY);
                startActivity(intent);
                //finish();
                //statusText.setText("SUCESSO no login");
            }

            @Override
            public void onCancel() {
                Log.d("onCancel",  "onCancel callback");
                System.out.println( "FACEBOOK login canceled");

                //statusText.setText("Login Cancelado");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("onError",  "onError callback");
                System.out.println( "Error on FACEBOOK login");
                //statusText.setText("ERRO no login");
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

}
