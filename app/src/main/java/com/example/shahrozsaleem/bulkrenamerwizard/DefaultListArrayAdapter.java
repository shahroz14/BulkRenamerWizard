package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by Shahroz Saleem on 15-Apr-17.
 */

public class DefaultListArrayAdapter extends ArrayAdapter<File> {

    private final Integer folderImg = R.drawable.folder;
    private final Integer fileImg = R.drawable.file;
    private final File[] files;
    private final Activity context;


    DefaultListArrayAdapter(Activity context, File[] files){

        super(context, R.layout.activity_my_file_list, files);
        this.files = files;
        this.context = context;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activity_my_file_list, null, true);


        ImageView img = (ImageView) rowView.findViewById(R.id.imageView);
        TextView tv = (TextView) rowView.findViewById(R.id.fileNameTV);
        if(files[position].isDirectory())
            img.setImageResource(R.drawable.folder);
        else
            img.setImageResource(R.drawable.file);

        tv.setText(files[position].getName());

        TextView modView = (TextView) rowView.findViewById(R.id.dateModifiedTV);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        modView.setText("Last Modified: "+sdf.format(files[position].lastModified()));

        TextView sizeView = (TextView) rowView.findViewById(R.id.fileSizeTV);
        if(!files[position].isDirectory())
            sizeView.setText("Size: "+ getSize(files[position].length()));
        else
            sizeView.setText("");

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
