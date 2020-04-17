package com.asd412id.bukutamu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    String _token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences configs;
        configs = getApplicationContext().getSharedPreferences("configs",MODE_PRIVATE);
        _token = configs.getString("_token","");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (_token.equals("")){
                    intent = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }else {
                    Toast.makeText(MainActivity.this,_token,Toast.LENGTH_LONG).show();
                }
            }
        },2500);
    }
}
