package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide toolbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //hide notificatoin bar

        setContentView(R.layout.activity_splash);
        makeDirectories();
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(1000);
                    Intent intent = new Intent(SplashActivity.this, ChooseUtilityActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };
        thread.start();

    }

    void makeDirectories(){
        File appRoot = new File(Environment.getExternalStorageDirectory(), String.valueOf("Bulk Rename Wizard"));
        appRoot.mkdirs();
        File wizard = new File(appRoot, "wizards");
        wizard.mkdirs();
    }

}
