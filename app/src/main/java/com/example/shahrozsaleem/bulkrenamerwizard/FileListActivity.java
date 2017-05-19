package com.example.shahrozsaleem.bulkrenamerwizard;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class FileListActivity extends AppCompatActivity {

    final static String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
    public final static int PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE=700; //App defined Int Constant to unquely identify permission


    ListView fileList;
    Intent in;

    static File internalStorage = Environment.getExternalStorageDirectory();
    File parent = internalStorage;
    File[] files;
    File wizardFile;

    int writePermission;
    int readPermission;

    CheckBoxListArrayAdapter fileAdapter;
    DefaultListArrayAdapter defaultFileAdapter;
    ActionBar actionBar;

    boolean menuOption = false;
    boolean duplicateMenu = false;
    int depth = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        files = new File[0];
        in = getIntent();

        if(in.getIntExtra("duplicateMenu",0)==1)
            duplicateMenu = true;

        actionBar = getSupportActionBar();
        actionBar.setTitle("Internal Storage");

        final String wizardFilePath = in.getStringExtra("WizardFilePath");

        if(wizardFilePath!=null)
            wizardFile = new File(in.getStringExtra("WizardFilePath"));

        if(! (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FileListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(FileListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                showPermissionRequiredDialog("Give application permissions to read and write to storage, needed browse and rename files", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(FileListActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE );
                    }
                 });
            }
            else {
                ActivityCompat.requestPermissions(FileListActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE );
            }

        } else {
            files = separateFilesAndFolders(parent.listFiles());
        }
        defaultFileAdapter = new DefaultListArrayAdapter(this, files);
        fileList = (ListView) findViewById(R.id.fileList);
        fileList.setAdapter(defaultFileAdapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(files[i].isDirectory()){
                    depth++;
                    parent = files[i];
                    actionBar.setTitle(parent.getName());
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    files = separateFilesAndFolders(parent.listFiles());
                    defaultFileAdapter.updateAdapter(files);
                }
                else {
                    Toast.makeText(FileListActivity.this, "Can't open file", Toast.LENGTH_SHORT).show();
                }

            }
        });


        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                menuOption  = true;
                invalidateOptionsMenu();
                fileAdapter = new CheckBoxListArrayAdapter(FileListActivity.this, files, i);
                actionBar.setDisplayHomeAsUpEnabled(false);
                fileList.setAdapter(fileAdapter);
                fileList.setSelection(i);

                return true;
            }
        });


    }




    public static File[] separateFilesAndFolders(File[] ff) {
        ArrayList<File> filesList = new ArrayList<File>();
        ArrayList<File> foldersList = new ArrayList<File>();
        for (File f: ff) {
            if(f.isDirectory())
                foldersList.add(f);
            else
                filesList.add(f);
        }
        FileComparator fc = new FileComparator();
        Collections.sort(filesList, fc);
        Collections.sort(foldersList, fc);
        foldersList.addAll(filesList);
        File[] f = new File[foldersList.size()];
        Iterator<File> itr = foldersList.iterator();
        int i =0;
        while(itr.hasNext()){
            f[i++] = itr.next();
        }
        return f;

    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        Log.d("Check", "Menu");
        if(menuOption) {
            if(duplicateMenu)
                menu.add("Find Duplicates").setIcon(R.drawable.dup_menu).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            else
                menu.add("Rename").setIcon(R.drawable.rename).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add("Cancel").setIcon(R.drawable.cross).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        else
            return false;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home){
            depth--;
            if(depth==0) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("Internal Storage");
                parent = internalStorage;
            }
            else {
                parent = parent.getParentFile();
                actionBar.setTitle(parent.getName());
            }
            files = separateFilesAndFolders(parent.listFiles());
            defaultFileAdapter.updateAdapter(files);
            return true;
        }

        if(item.toString().equals("Cancel")){
            menuOption = false;
            invalidateOptionsMenu();
            actionBar.setDisplayHomeAsUpEnabled(true);
            fileList.setAdapter(defaultFileAdapter);
        }

        if(item.toString().equals("Rename")){

            ArrayList<File> files = getSelectedFiles();
            ArrayList<File> folders = getSelectedFolders();
            Renamer renamer = new Renamer(internalStorage, folders, files, wizardFile, getApplicationContext());
            renamer.rename();
            finish();
            Toast.makeText(this, "Files Renamed", Toast.LENGTH_LONG).show();

        }

        if(item.toString().equals("Find Duplicates")) {


            ArrayList<File> files = getSelectedFiles();
            ArrayList<File> folders = getSelectedFolders();
            DuplicateFileRemover dfr = new DuplicateFileRemover(folders, files);
            try {
                dfr.findDuplicates();

                Intent intent = new Intent(FileListActivity.this, DuplicateFileRemoverActivity.class);
                intent.putExtra("duplicateFiles", dfr.duplicateFiles);
                intent.putExtra("savedSpace", dfr.savedSpace);
                intent.putExtra("duplicatesCount", dfr.dupCount);

                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        Log.d("Check", item.getItemId()+" "+item.toString());
        return super.onOptionsItemSelected(item);
    }



    ArrayList<java.io.File> getSelectedFiles(){
        ArrayList<File> fileArrayList = new ArrayList<File>();

        boolean[] marked = fileAdapter.getAllMarkedFiles();
        for(int i=0; i<marked.length; i++){
            if(marked[i] && files[i].isFile()){
                fileArrayList.add(files[i]);
            }
        }
        return fileArrayList;
    }

    ArrayList<java.io.File> getSelectedFolders(){
        ArrayList<File> folderArrayList = new ArrayList<File>();

        boolean[] marked = fileAdapter.getAllMarkedFiles();
        for(int i=0; i<marked.length; i++){
            if(marked[i] && files[i].isDirectory()){
                folderArrayList.add(files[i]);
            }
        }
        return folderArrayList;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_WRITE_EXTERNAL_STORAGE:
                if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    files = separateFilesAndFolders(parent.listFiles());
                    defaultFileAdapter.updateAdapter(files);
                }
                else {
                    finish();
                    Toast.makeText(FileListActivity.this, "Permission denied By user, application cannot work without permissions", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(FileListActivity.this, "Permission denied By user, Application needs Permissions to function", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }




}


class FileComparator implements Comparator<File>{

    @Override
    public int compare(File f1, File f2) {
        String f1N = f1.getName().toLowerCase();
        String f2N = f2.getName().toLowerCase();
        return f1N.compareTo(f2N);
    }
}