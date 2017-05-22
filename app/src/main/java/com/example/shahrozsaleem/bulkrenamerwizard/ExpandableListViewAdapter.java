package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shahroz Saleem on 17-May-17.
 */

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    HashMap<File, List<File>> duplicateFiles;
    Activity context;
    List<File> groupHeader;


    ExpandableListViewAdapter(Activity context, HashMap<File, List<File>> dupFiles){
        this.context = context;
        this.duplicateFiles = dupFiles;
        groupHeader = new ArrayList<File>();
        setGroupHeader();


    }

    private void setGroupHeader() {
        Iterator<Map.Entry<File, List<File>>> itr =  duplicateFiles.entrySet().iterator();
        while(itr.hasNext()){
            groupHeader.add(itr.next().getKey());
        }
    }

    @Override
    public int getGroupCount() {
        return groupHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return duplicateFiles.get(groupHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return duplicateFiles.get(groupHeader.get(i));
    }

    @Override
    public Object getChild(int i, int i1) {
        return duplicateFiles.get(groupHeader.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int group, int child) {
        return child;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPos, boolean b, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.parent_layout, viewGroup, false);
        TextView fileName = (TextView) view.findViewById(R.id.fileNameTV);
        TextView fileSize = (TextView) view.findViewById(R.id.fileSizeTV);
        TextView parentPath = (TextView) view.findViewById(R.id.parentPathTV);
        fileName.setText(groupHeader.get(groupPos).getName());
        fileSize.setText(DefaultListArrayAdapter.getSize(groupHeader.get(groupPos).length()));
        parentPath.setText(groupHeader.get(groupPos).getParentFile().toString());

        return view;
    }

    @Override
    public View getChildView(final int group, final int child, boolean lastChild, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.child_layout, viewGroup,false);
        TextView fileName = (TextView) view.findViewById(R.id.fileNameTV);
        TextView parentPath = (TextView) view.findViewById(R.id.parentPathTV);
        ImageView deleteBtn = (ImageView) view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                File toDelete = duplicateFiles.get(groupHeader.get(group)).get(child);
                DuplicateFileRemoverActivity.fireChange(DuplicateFileRemoverActivity.dupCount-1, (long)(DuplicateFileRemoverActivity.savedSpace-toDelete.length()));
                toDelete.delete();
                duplicateFiles.get(groupHeader.get(group)).remove(child);
                if(duplicateFiles.get(groupHeader.get(group)).size() == 0){
                    duplicateFiles.remove(groupHeader.get(group));
                    groupHeader.remove(group);
                }
                notifyDataSetChanged();
            }
        });

        fileName.setText(duplicateFiles.get(groupHeader.get(group)).get(child).getName());
        parentPath.setText(duplicateFiles.get(groupHeader.get(group)).get(child).getParentFile().toString());

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
