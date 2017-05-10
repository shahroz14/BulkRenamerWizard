package com.example.shahrozsaleem.bulkrenamerwizard;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenamerWizardActivity extends AppCompatActivity {

    private File appRoot;
    private File wizardRoot;
    private CheckBox addPrefixCB;
    private EditText prefixET;
    private CheckBox addSuffixCB;
    private EditText suffixET;
    private CheckBox prefixNumCB;
    private  EditText startFromET;
    private RadioGroup prefixNumRG;
    private RadioButton preNumericRB;
    private RadioButton preAlphaRB;
    private RadioButton preRomanRB;
    private CheckBox sufNumCB;
    private EditText sufStartFromET;
    private RadioGroup suffixNumRG;
    private RadioButton sufNumericRB;
    private RadioButton sufAlphaRB;
    private RadioButton sufRomanRB;
    private CheckBox replaceCB;
    private EditText replaceStringET;
    private EditText replaceWithET;
    private CheckBox allUppercaseCB;
    private CheckBox allLowercaseCB;
    private  CheckBox firstLetterCapitalCB;
    private CheckBox removeCB;
    private EditText betweenET;
    private EditText andET;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        makeDirectories();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_renamer_wizard);
        addPrefixCB = (CheckBox) findViewById(R.id.prefixCB);
        prefixET = (EditText) findViewById(R.id.prefixET);
        addSuffixCB = (CheckBox) findViewById(R.id.suffixCB);
        suffixET = (EditText) findViewById(R.id.suffixET);
        prefixNumCB = (CheckBox) findViewById(R.id.preNumCB);
        startFromET = (EditText) findViewById(R.id.startFromET);
        prefixNumRG = (RadioGroup) findViewById(R.id.preNumRG);
        preNumericRB = (RadioButton) findViewById(R.id.preNumRB);
        preAlphaRB = (RadioButton) findViewById(R.id.preAlphaRB);
        preRomanRB = (RadioButton) findViewById(R.id.preRomanRB);
        sufNumCB = (CheckBox) findViewById(R.id.sufNumCB);
        sufStartFromET = (EditText) findViewById(R.id.suffixStartFromET);
        suffixNumRG = (RadioGroup) findViewById(R.id.sufNumRG);
        sufNumericRB = (RadioButton) findViewById(R.id.sufNumRB);
        sufAlphaRB = (RadioButton) findViewById(R.id.sufAlphaRB);
        sufRomanRB = (RadioButton) findViewById(R.id.sufRomanRB);
        replaceCB = (CheckBox) findViewById(R.id.replaceStringCB);
        replaceStringET = (EditText) findViewById(R.id.replaceStringET);
        replaceWithET = (EditText) findViewById(R.id.replaceWithET);
        allUppercaseCB = (CheckBox) findViewById(R.id.allUpperCaseCB);
        allLowercaseCB = (CheckBox) findViewById(R.id.allLowerCaseCB);
        firstLetterCapitalCB = (CheckBox) findViewById(R.id.firstLetterCapitalCB);
        removeCB = (CheckBox) findViewById(R.id.removeCB);
        betweenET = (EditText) findViewById(R.id.betweenET);
        andET = (EditText) findViewById(R.id.andET);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.wizard_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.toString().equals("Save")){
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            final View dialog = getLayoutInflater().inflate(R.layout.file_save_dialog, null);
            mBuilder.setView(dialog);
            final AlertDialog alertDialog = mBuilder.create();
            alertDialog.show();

            final EditText fileNameET = (EditText) dialog.findViewById(R.id.fileNameET);


            Button saveBtn = (Button) dialog.findViewById(R.id.saveBtn);
            saveBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    try {
                        generateFile(new File(wizardRoot, String.valueOf(fileNameET.getText()+".brw")));
                        Toast.makeText(dialog.getContext(), "File Saved", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);
            cancelBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });

        }

        if(item.toString().equals("Go")){

            File wizardTempFile = new File(wizardRoot, "temp.brw");
            try {
                generateFile(wizardTempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(isEmptyField()){
                return true;
            }

            Intent intent = new Intent(RenamerWizardActivity.this, FileListActivity.class);
            intent.putExtra("WizardFilePath", wizardTempFile.getPath());
            startActivity(intent);


        }
        return true;
    }

    boolean isEmptyField(){
        if(prefixNumCB.isChecked() && TextUtils.isEmpty(startFromET.getText().toString())) {
            startFromET.setError(getString(R.string.blankField));
            return true;
        }
        if(sufNumCB.isChecked() && TextUtils.isEmpty(sufStartFromET.getText().toString())) {
            sufStartFromET.setError(getString(R.string.blankField));
            return true;
        }

        if(replaceCB.isChecked() && TextUtils.isEmpty(replaceStringET.getText().toString())) {
            replaceStringET.setError(getString(R.string.blankField));
            return true;
        }
        if(removeCB.isChecked() && (TextUtils.isEmpty(betweenET.getText().toString())
                                            ||TextUtils.isEmpty(andET.getText().toString()))) {
            if(TextUtils.isEmpty(betweenET.getText().toString()))
                betweenET.setError(getString(R.string.blankField));
            if(TextUtils.isEmpty(andET.getText().toString()))
                andET.setError(getString(R.string.blankField));
            return true;
        }
        return false;
    }

    void makeDirectories(){
        File appRoot = new File(Environment.getExternalStorageDirectory(), String.valueOf("Bulk Rename Wizard"));
        appRoot.mkdirs();
        this.appRoot = appRoot;
        File wizard = new File(appRoot, "wizards");
        wizard.mkdirs();
        wizardRoot = wizard;

    }

    void generateFile(File file) throws IOException {

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        Wizard addPre = new Wizard();
        if(addPrefixCB.isChecked()){
            addPre.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(prefixET.getText()+"");
            addPre.setParams(params);
        }
        oos.writeObject(addPre);

        Wizard addSuf = new Wizard();
        if(addSuffixCB.isChecked()){
            addSuf.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(suffixET.getText()+"");
            addPre.setParams(params);
        }
        oos.writeObject(addSuf);

        Wizard preNum = new Wizard();
        if(prefixNumCB.isChecked()){
            preNum.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(startFromET.getText()+"");
            long checkedId = prefixNumRG.getCheckedRadioButtonId();
            if(checkedId==preNumericRB.getId()){
                params.add("Numeric");
            }
            else if(checkedId==preAlphaRB.getId()){
                params.add("Alpha");
            }
            else if(checkedId==preRomanRB.getId()){
                params.add("Roman");
            }
            preNum.setParams(params);
        }
        oos.writeObject(preNum);


        Wizard sufNum = new Wizard();
        if(sufNumCB.isChecked()){
            sufNum.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(sufStartFromET.getText()+"");
            long checkedId = suffixNumRG.getCheckedRadioButtonId();
            if(checkedId==sufNumericRB.getId()){
                params.add("Numeric");
            }
            else if(checkedId==sufAlphaRB.getId()){
                params.add("Alpha");
            }
            else if(checkedId==sufRomanRB.getId()){
                params.add("Roman");
            }
            sufNum.setParams(params);
        }
        oos.writeObject(sufNum);


        Wizard replace = new Wizard();
        if(replaceCB.isChecked()){
            replace.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(String.valueOf(replaceStringET.getText()));
            params.add(String.valueOf(replaceWithET.getText()));
            replace.setParams(params);
        }
        oos.writeObject(replace);

        Wizard allUppercase = new Wizard();
        if(allUppercaseCB.isChecked()){
            allUppercase.setChecked(true);
        }
        oos.writeObject(allUppercase);


        Wizard allLowercase = new Wizard();
        if(allLowercaseCB.isChecked()){
            allLowercase.setChecked(true);
        }
        oos.writeObject(allLowercase);


        Wizard firstLetterCapital = new Wizard();
        if(firstLetterCapitalCB.isChecked()){
            firstLetterCapital.setChecked(true);
        }
        oos.writeObject(firstLetterCapital);


        Wizard remove = new Wizard();
        if(removeCB.isChecked()){
            remove.setChecked(true);
            List<String> params = new ArrayList<String>();
            params.add(String.valueOf(betweenET.getText()));
            params.add(String.valueOf(andET.getText()));
        }
        oos.writeObject(remove);

        oos.flush();
        oos.close();


    }

}
