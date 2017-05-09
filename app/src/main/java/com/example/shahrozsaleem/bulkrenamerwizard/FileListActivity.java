package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
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
import java.util.Iterator;
import java.util.List;

public class FileListActivity extends AppCompatActivity {

    ListView fileList;
    static File root = Environment.getExternalStorageDirectory();
    Intent in;
    File[] files;

    StringBuilder currFolder = new StringBuilder(root.getPath());
    boolean menuOption = false;
    File wizardFile;
    CheckBoxListArrayAdapter fileAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide toolbar
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_file_list);


        in = getIntent();
        String str = in.getStringExtra("newFiles");
        final String wizardFilePath = in.getStringExtra("WizardFilePath");
        final String currentFolder = in.getStringExtra("CurrentFolder");


        if(wizardFilePath!=null)
            wizardFile = new File(in.getStringExtra("WizardFilePath"));

        //Toast.makeText(this, wizardFilePath+"", Toast.LENGTH_LONG).show();


        if(str!=null)
            root = new File(str);




        files = separateFilesAndFolders(root.listFiles());
        if( files.length > 0) {

            Log.d("debug", files.length + "------> " + files[0]);
            DefaultListArrayAdapter fileAdapter = new DefaultListArrayAdapter(this, files);
            fileList = (ListView) findViewById(R.id.fileList);
            fileList.setAdapter(fileAdapter);

        }


        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(FileListActivity.this, FileListActivity.class);
                if(files[i].listFiles().length==0){
                    intent = new Intent(FileListActivity.this, EmptyFolderActivity.class);
                }

                intent.putExtra("newFiles", files[i].toString());
                intent.putExtra("WizardFilePath", wizardFilePath);
                startActivity(intent);
                Log.d("Debasd", "Aftre start");
                //Toast.makeText(FileListActivity.this, "you clicked "+files[i].getName(), Toast.LENGTH_SHORT).show();
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
        if(item.toString().equals("Cancel")){
            menuOption = false;
            invalidateOptionsMenu();
            DefaultListArrayAdapter fileAdapter = new DefaultListArrayAdapter(this, files);
            fileList = (ListView) findViewById(R.id.fileList);
            fileList.setAdapter(fileAdapter);
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
