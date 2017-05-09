package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by Shahroz Saleem on 01-May-17.
 */

public class CheckBoxListArrayAdapter extends ArrayAdapter<File> {

    private final Integer folderImg = R.drawable.folder;
    private final Integer fileImg = R.drawable.file;
    private final File[] files;
    private boolean[] isChecked;
    private final Activity context;
    private int checkedItem;

    CheckBoxListArrayAdapter(Activity context, File[] files, int checkedItem){

        super(context, R.layout.long_click_layout, files);
        this.files = files;
        this.context = context;
        this.checkedItem = checkedItem;
        this.isChecked = new boolean[files.length];
        this.isChecked[checkedItem] = true;
    }

    boolean[] getAllMarkedFiles(){
        return isChecked;
    }

    static class ViewHolder {
        CheckBox checkBox;
        ImageView icon;
        TextView fileName;
        TextView fileSize;
        TextView lastMod;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        final ViewHolder viewHolder = new ViewHolder();

        LayoutInflater inflater = context.getLayoutInflater();
        rowView = inflater.inflate(R.layout.long_click_layout, null, true);


        viewHolder.icon = (ImageView) rowView.findViewById(R.id.imageView);
        viewHolder.fileName = (TextView) rowView.findViewById(R.id.fileNameTV);
        viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.checkboxBtn);
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                CheckBoxListArrayAdapter.this.isChecked[position] = !CheckBoxListArrayAdapter.this.isChecked[position];
            }
        });


        viewHolder.checkBox.setChecked(this.isChecked[position]);


        if(files[position].isDirectory())
            viewHolder.icon.setImageResource(R.drawable.folder);
        else
            viewHolder.icon.setImageResource(R.drawable.file);


        viewHolder.fileName.setText(files[position].getName());
        viewHolder.lastMod = (TextView) rowView.findViewById(R.id.dateModifiedTV);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        viewHolder.lastMod.setText("Last Modified: "+sdf.format(files[position].lastModified()));

        viewHolder.fileSize = (TextView) rowView.findViewById(R.id.fileSizeTV);

        if(!files[position].isDirectory())
            viewHolder.fileSize.setText("Size: "+ getSize(files[position].length()));
        else
            viewHolder.fileSize.setText("");


        return rowView;
    }


    static String getSize(long size) {

        String fs = "";
        double inKB = size/1024.0;
        double inMB = inKB/1024.0;
        double inGB = inMB/1024.0;
        DecimalFormat df = new DecimalFormat("#0.##");
        if(inGB > 1){
            fs = df.format(inGB)+" GB";
            return fs;
        }
        else if(inMB > 1){
            fs = df.format(inMB)+" MB";
            return fs;
        }
        else if(inKB > 0.1){
            fs = df.format(inKB)+" KB";
            return fs;
        }
        return size+" B";

    }

    @Override
    public String toString() {
        return Arrays.toString(files);
    }


}
