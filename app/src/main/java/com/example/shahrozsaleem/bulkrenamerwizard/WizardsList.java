package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
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

public class WizardsList extends AppCompatActivity {


    ListView listView ;
    File[] wizardFiles;
    public static File wizardFilesMainFolder;
    Button viewBtn;
    Button deleteBtn;
    Button renameBtn;
    ActionBar actionBar;
    int selectedPosition = -1;
    WizardsListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizards_list);
        actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle("Wizards List");
        wizardFilesMainFolder = new File( Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name), "wizards");
        listView = (ListView) findViewById(R.id.wizardFileList);
        wizardFiles = wizardFilesMainFolder.listFiles();
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
                    adapter = new WizardsListAdapter(WizardsList.this, wizardFiles);
                    listView.setAdapter(adapter);
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
                            adapter = new WizardsListAdapter(WizardsList.this, wizardFiles);
                            listView.setAdapter(adapter);
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
