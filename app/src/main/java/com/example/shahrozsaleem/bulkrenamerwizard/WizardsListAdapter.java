package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Shahroz Saleem on 14-May-17.
 */

public class WizardsListAdapter extends ArrayAdapter<File> {

    File[] wizardFiles;
    private final Activity context;
    int selectedPosition = -1;

    public WizardsListAdapter(Activity context, File[] wizardFiles) {
        super(context, R.layout.wizard_list_item);
        this.context = context;
        this.wizardFiles = wizardFiles;
        if( wizardFiles!=null && wizardFiles.length > 0)
            Arrays.sort(wizardFiles, new FileComparator());
    }


    @Override
    public int getCount(){
        if(wizardFiles==null)
            return  0;
        return wizardFiles.length;
    }

    void updateAdapter(File[] newFiles) {
        wizardFiles = newFiles;
        Arrays.sort(wizardFiles, new FileComparator());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.wizard_list_item, null);
        TextView fileName = (TextView) rowView.findViewById(R.id.fileNameTV);
        fileName.setText(Renamer.removeExtensionFromFile(wizardFiles[i].getName().toString()));
        if(i==selectedPosition){
            rowView.setBackgroundResource(R.drawable.round_edittext);
        }
        return rowView;

    }
}
