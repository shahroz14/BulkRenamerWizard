package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FileListActivity extends AppCompatActivity {

    ListView fileList;
    Intent in;

    static File root = Environment.getExternalStorageDirectory();
    File parent = root;
    File[] files;
    File wizardFile;

    CheckBoxListArrayAdapter fileAdapter;
    DefaultListArrayAdapter defaultFileAdapter;
    ActionBar actionBar;

    boolean menuOption = false;
    int depth = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        in = getIntent();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Internal Storage");

        final String wizardFilePath = in.getStringExtra("WizardFilePath");

        if(wizardFilePath!=null)
            wizardFile = new File(in.getStringExtra("WizardFilePath"));

        files = separateFilesAndFolders(root.listFiles());
        defaultFileAdapter = new DefaultListArrayAdapter(this, files);
        fileList = (ListView) findViewById(R.id.fileList);
        fileList.setAdapter(defaultFileAdapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                depth++;
                parent = files[i];
                actionBar.setTitle(parent.getName());
                actionBar.setDisplayHomeAsUpEnabled(true);
                files = separateFilesAndFolders(files[i].listFiles());
                defaultFileAdapter.updateAdapter(files);

            }
        });


        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                menuOption  = true;
                invalidateOptionsMenu();
                fileAdapter = new CheckBoxListArrayAdapter(FileListActivity.this, files, i);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        Log.d("Check", "Menu");
        if(menuOption) {
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
                parent = root;
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
            fileList.setAdapter(defaultFileAdapter);
        }

        if(item.toString().equals("Rename")){

            ArrayList<File> files = getSelectedFiles();
            ArrayList<File> folders = getSelectedFolders();
            Renamer renamer = new Renamer(root, folders, files, wizardFile, getApplicationContext());
            renamer.rename();
            Intent in = getIntent();
            finish();
            startActivity(in);
            Toast.makeText(this, "Files Renamed", Toast.LENGTH_LONG).show();

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
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;
        if(item.getTitle()=="Rename") {
            File f = files[listPosition];
            if(!f.isDirectory())
                Toast.makeText(this, f.getPath()+"not a directory", Toast.LENGTH_SHORT).show();
            else{
                Renamer r = new Renamer(f.getPath());
                r.rename();
                Toast.makeText(this, "Renamed Successfully", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }


}
