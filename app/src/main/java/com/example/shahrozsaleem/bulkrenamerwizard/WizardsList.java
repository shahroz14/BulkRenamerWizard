package com.example.shahrozsaleem.bulkrenamerwizard;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class WizardsList extends AppCompatActivity {


    ListView listView ;
    File[] wizardFiles;
    public static File wizardFilesMainFolder;
    public final static int PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE=700; //App defined Int Constant to unquely identify permission

    Button viewBtn;
    Button deleteBtn;
    Button renameBtn;
    ActionBar actionBar;
    int selectedPosition = -1;
    WizardsListAdapter adapter;
    int readPermission;
    int writePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizards_list);
        actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle("Wizards List");

        readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if(! (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(WizardsList.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                showPermissionRequiredDialog("Give application permissions to read and write to storage, needed browse and rename files", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(WizardsList.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE );
                    }
                });
            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE );
            }
        }
        else {
            wizardFilesMainFolder = new File( Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name), "wizards");
            wizardFiles = wizardFilesMainFolder.listFiles();
            Arrays.sort(wizardFiles, new FileComparator());
        }

        listView = (ListView) findViewById(R.id.wizardFileList);
        viewBtn = (Button) findViewById(R.id.viewBtn);
        renameBtn = (Button) findViewById(R.id.renameBtn);
        deleteBtn= (Button) findViewById(R.id.deleteBtn);
        adapter = new WizardsListAdapter(this, wizardFiles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.selectedPosition = i;
                selectedPosition = i;
                adapter.notifyDataSetChanged();
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(selectedPosition<0){
                    Toast.makeText(WizardsList.this, "No wizard selected", Toast.LENGTH_SHORT).show();
                }
                else if(wizardFiles[selectedPosition].getName().equals("temp.brw")){
                    Toast.makeText(WizardsList.this, "Can't view", Toast.LENGTH_SHORT).show();
                }
                else {
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(WizardsList.this);
                    final View dialog = WizardsList.this.getLayoutInflater().inflate(R.layout.view_wizard_file, null);
                    TextView wizardDescTV = (TextView) dialog.findViewById(R.id.wizardDescTV);
                    wizardDescTV.setMovementMethod(new ScrollingMovementMethod());
                    ViewWizard vW = new ViewWizard(WizardsList.this, wizardFiles[selectedPosition]);
                    wizardDescTV.setText(vW.getWizardDesc());
                    Log.d("Debug", "wizard "+wizardDescTV.getText().toString());
                    Log.d("Debug", "text"+vW.getWizardDesc());
                    mBuilder.setView(dialog);
                    final AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(selectedPosition<0){
                    Toast.makeText(WizardsList.this, "No Wizard Selected", Toast.LENGTH_SHORT).show();
                }
                else if(wizardFiles[selectedPosition].getName().equals("temp.brw")){
                    Toast.makeText(WizardsList.this, "Can't delete this file", Toast.LENGTH_SHORT).show();
                }
                else {
                    wizardFiles[selectedPosition].delete();
                    Toast.makeText(WizardsList.this, "Deleted", Toast.LENGTH_SHORT).show();
                    wizardFiles = wizardFilesMainFolder.listFiles();
                    Arrays.sort(wizardFiles, new FileComparator());
                    adapter.updateAdapter(wizardFiles);
                }
            }
        });


        renameBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(selectedPosition<0){
                    Toast.makeText(WizardsList.this, "No wizard selected", Toast.LENGTH_SHORT).show();
                }
                else if(wizardFiles[selectedPosition].getName().equals("temp.brw")){
                    Toast.makeText(WizardsList.this, "Can't rename", Toast.LENGTH_SHORT).show();
                }
                else{
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(WizardsList.this);
                    final View dialog = WizardsList.this.getLayoutInflater().inflate(R.layout.file_rename_dialog, null);
                    final EditText fileNameET = (EditText) dialog.findViewById(R.id.fileNameET);
                    fileNameET.setText(Renamer.removeExtensionFromFile(wizardFiles[selectedPosition].getName()));
                    mBuilder.setView(dialog);
                    final AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();
                    Button rBtn = (Button) dialog.findViewById(R.id.renameBtn);
                    rBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            wizardFiles[selectedPosition].renameTo(new File(wizardFiles[selectedPosition].getParent(), fileNameET.getText().toString()));
                            Toast.makeText(WizardsList.this, "Renamed", Toast.LENGTH_SHORT).show();
                            wizardFiles = wizardFilesMainFolder.listFiles();
                            Arrays.sort(wizardFiles, new FileComparator());
                            adapter.updateAdapter(wizardFiles);
                        }
                    });

                    Button cBtn = (Button) dialog.findViewById(R.id.cancelBtn);
                    cBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Debug", "onResume Called");
        wizardFiles = wizardFilesMainFolder.listFiles();
        Arrays.sort(wizardFiles, new FileComparator());
        adapter = new WizardsListAdapter(this, wizardFiles);
        listView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE:
                if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    wizardFilesMainFolder = new File( Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name), "wizards");
                    listView = (ListView) findViewById(R.id.wizardFileList);
                    wizardFiles = wizardFilesMainFolder.listFiles();
                    Arrays.sort(wizardFiles, new FileComparator());
                }
                else {
                    finish();
                    Toast.makeText(getApplicationContext(), "Permission denied By user, application cannot work without permissions", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    void showPermissionRequiredDialog(String message, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Permissions Required")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Grant", positiveListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        Toast.makeText(WizardsList.this, "Permission denied By user, Application needs Permissions to function", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wizard_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.action_add:
                intent = new Intent(this, RenamerWizardActivity.class);
                startActivity(intent);
                break;
            case R.id.action_continue:
                if(selectedPosition<0){
                    Toast.makeText(this, "Select a wizard file.", Toast.LENGTH_SHORT).show();
                    break;
                }

                File wizardFile = wizardFiles[selectedPosition];
                intent = new Intent(this, FileListActivity.class);
                intent.putExtra("WizardFilePath", wizardFile.getPath());
                startActivity(intent);
                break;
        }
        return true;
    }


}
