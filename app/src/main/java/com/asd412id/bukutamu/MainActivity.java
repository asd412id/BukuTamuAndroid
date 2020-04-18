package com.asd412id.bukutamu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    SharedPreferences configs;
    String _token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configs = getApplicationContext().getSharedPreferences("configs",MODE_PRIVATE);
        _token = configs.getString("_token","");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (_token.equals("")){
                    intent = new Intent(MainActivity.this, RegistrationActivity.class);
                }else {
                    String getInstansi = configs.getString("guest",null);
                    if (getInstansi==null){
                        intent = new Intent(MainActivity.this, HomeActivity.class);
                    }else{
                        intent = new Intent(MainActivity.this, RatingActivity.class);
                    }
                }
                startActivity(intent);
                finishAffinity();
            }
        },2500);
    }
}
