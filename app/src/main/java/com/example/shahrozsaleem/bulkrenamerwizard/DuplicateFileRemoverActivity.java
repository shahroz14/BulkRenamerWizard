package com.example.shahrozsaleem.bulkrenamerwizard;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

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
        for(int i=0; i<files.size()-1; i++) {
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



            /*File file1 = files.get(i);
            dupFiles = new ArrayList<File>();
            for(int j=i+1; j<files.size(); j++) {
                File file2 = files.get(j);
                if(isDuplicate(file1, file2)) {
                    dupCount++;
                    dupFiles.add(file2);
                    savedSpace += (file2.length());
                    files.remove(j);
                    j--;
                }
            }
            if(!dupFiles.isEmpty())
                duplicateFiles.put(file1, dupFiles);*/

        Iterator<Map.Entry<Long, List<File>>> itr = map.entrySet().iterator();
        while (itr.hasNext()){
            Map.Entry<Long, List<File>> entry = itr.next();
            List<File> list = entry.getValue();

            for(int i=0; i<list.size()-1; i++){
                dupFiles = new ArrayList<File>();
                File file1 = list.get(i);
                for(int j=i+1; j<list.size(); j++) {
                    File file2 = list.get(j);
                    if(isDuplicate(file1, file2)) {
                        dupCount++;
                        dupFiles.add(file2);
                        savedSpace += (file2.length());
                        list.remove(j);
                        j--;
                    }
                }
                if(!dupFiles.isEmpty()){
                    duplicateFiles.put(file1, dupFiles);
                }
            }
        }

    }


    private boolean isDuplicate(File f1, File f2) throws IOException{

        if(f1.isDirectory()|| f2.isDirectory())
            return false;

        if(f1.length()!=f2.length())
            return false;

        BufferedReader br1 = new BufferedReader(new FileReader(f1));
        BufferedReader br2 = new BufferedReader(new FileReader(f2));

        char[] buff1 = new char[FILE_COMP];
        char[] buff2 = new char[FILE_COMP];

        if(f2.length()<=FILE_COMP) {
            br1.read(buff1);
            br2.read(buff2);

            for(int i=0; i<FILE_COMP; i++){
                if(buff1[i]!=buff2[i]) {
                    br1.close();
                    br2.close();
                    return false;
                }
            }
        }

        else {

            int[] nos = new int[FILE_COMP];
            for(int i=0; i<FILE_COMP; i++)
                nos[i] = i;

            while(br1.read(buff1)!=-1){
                br2.read(buff2);

                for(int i=0; i<FILE_COMP; i++){
                    if(buff1[i]!=buff2[i]){
                        br1.close();
                        br2.close();
                        return false;
                    }
                }
            }

        }
        br1.close();
        br2.close();
        return true;
    }

}
