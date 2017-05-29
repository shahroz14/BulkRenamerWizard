package com.example.shahrozsaleem.bulkrenamerwizard;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DuplicateFileRemoverActivity extends AppCompatActivity {

    HashMap<File, List<File>> duplicateFiles;
    static int dupCount;
    static double savedSpace;
    static TextView dupCountTV ;
    static TextView savedSpaceTV;
    ExpandableListView expandableListView;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplicate_file_remover);
        actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle("Report");
        duplicateFiles = (HashMap<File, List<File>>) getIntent().getSerializableExtra("duplicateFiles");
        dupCount = getIntent().getIntExtra("duplicatesCount", 0);
        savedSpace = getIntent().getDoubleExtra("savedSpace", 0.0d);
        expandableListView = (ExpandableListView) findViewById(R.id.expandListView);
        dupCountTV = (TextView) findViewById(R.id.dupFoundTV);
        savedSpaceTV = (TextView) findViewById(R.id.spaceSavedTV);
        dupCountTV.setText(dupCount+" Duplicates Found");
        savedSpaceTV.setText(DefaultListArrayAdapter.getSize((long) savedSpace)+" can be saved");

        Log.d("Debug", expandableListView.toString());
        ExpandableListViewAdapter exAdapter = new ExpandableListViewAdapter(this, duplicateFiles);
        expandableListView.setAdapter(exAdapter);




    }

    @Override
    public void onBackPressed() {
        finish();
    }

    static void fireChange(int dupCount, long savedSpace){
        DuplicateFileRemoverActivity.dupCount = dupCount;
        DuplicateFileRemoverActivity.savedSpace = savedSpace;
        dupCountTV.setText(dupCount+" Duplicates Found");
        savedSpaceTV.setText(DefaultListArrayAdapter.getSize((long) savedSpace)+" saved");
    }


}


class DuplicateFileRemover {

    List<File> folders;
    List<File> files;
    int dupCount;
    double savedSpace;

    HashMap<File, List<File>> duplicateFiles;
    public static final int FILE_COMP = 2048;


    public DuplicateFileRemover(List<File> folders, List<File> files){
        this.files = files;
        this.folders = folders;
        duplicateFiles = new HashMap<File, List<File>>();
        collectAllFiles();
    }

    public void collectAllFiles(){
        while(!folders.isEmpty()){
            collectAllFromFolder(folders.get(0));
            folders.remove(0);
        }
    }


    public void collectAllFromFolder(File folder){
        File[] contents = folder.listFiles();
        for (File file : contents) {
            if(file.isDirectory() && !file.isHidden())
                folders.add(file);
            if(file.isFile() && !file.isHidden())
                files.add(file);
        }
    }

    public void findDuplicates() throws IOException {

        List<File> dupFiles;
        HashMap<Long, List<File>> map = new HashMap<>();

        for(int i=0; i<files.size(); i++) {
            File f = files.get(i);
            long len = f.length();
            if (!map.containsKey(len)) {
                List<File> filesOfSameSize = new ArrayList<>();
                filesOfSameSize.add(f);
                map.put(len, filesOfSameSize);
            } else {
                map.get(len).add(f);
            }
        }

        Iterator<Map.Entry<Long, List<File>>> itr = map.entrySet().iterator();

        while (itr.hasNext()){
            Map.Entry<Long, List<File>> entry = itr.next();
            List<File> list = entry.getValue();

            for(int i=0; i<list.size()-1; i++){
                dupFiles = new ArrayList<File>();
                File file1 = list.get(i);
                Boolean isDupFound = false;
                Log.d("Debug", file1.length()+"");
                for(int j=i+1; j<list.size(); j++) {
                    File file2 = list.get(j);
                    if(FileUtils.contentEquals(file1, file2)) {
                        dupCount++;
                        dupFiles.add(file2);
                        savedSpace += (file2.length());
                        list.remove(j);
                        isDupFound = true;
                        j--;
                    }
                }
                if(isDupFound)
                    i--;

                if(!dupFiles.isEmpty()){
                    duplicateFiles.put(file1, dupFiles);
                }
            }
        }

    }


}
