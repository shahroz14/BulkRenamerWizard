package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;

public class ChooseUtilityActivity extends AppCompatActivity {

    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_utility);
        actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle("Choose Utility");
        makeDirectories();

    }

    void makeDirectories(){
        File appRoot = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        appRoot.mkdirs();
        File wizard = new File(appRoot, "wizards");
        wizard.mkdirs();
        wizard = new File(appRoot, "text_files");
        wizard.mkdirs();
    }

    public void goToOCR(View view){
        Intent intent = new Intent(ChooseUtilityActivity.this, TextRecognitionActivity.class);
        startActivity(intent);
    }

    public void goToFileRenamer(View view) {
        Intent intent = new Intent(ChooseUtilityActivity.this, WizardsList.class);
        startActivity(intent);
    }

    public void goToDuplicateFileRemover(View view) {
        Intent intent = new Intent(ChooseUtilityActivity.this, FileListActivity.class);
        intent.putExtra("duplicateMenu", 1);
        startActivity(intent);
    }
}
