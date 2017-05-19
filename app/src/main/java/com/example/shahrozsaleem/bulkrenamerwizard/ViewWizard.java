package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shahroz Saleem on 14-May-17.
 */

public class ViewWizard {

    File wizardFile;
    List<Wizard> wizardList;
    Activity context;


    public ViewWizard(Activity context, File wizardFile) {

        this.wizardFile = wizardFile;
        wizardList = new ArrayList<>();
        readWizard();
        this.context = context;

    }

    private void readWizard(){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(wizardFile);
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong with wizard file.", Toast.LENGTH_LONG).show();
        }
        Wizard wiz;
        try {
            while((wiz = (Wizard) ois.readObject())!= null){
                wizardList.add(wiz);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getWizardDesc(){
        StringBuilder desc = new StringBuilder();
        Wizard addPre = wizardList.get(0);
        if(addPre.isChecked()){
            desc.append("\n\n# \'"+addPre.getParams().get(0)+"\' will be added before each file name. ");
        }
        Wizard addSuf = wizardList.get(1);
        if(addSuf.isChecked()){
            desc.append("\n\n# \'"+addSuf.getParams().get(0)+"\' will be added after each file name. ");
        }

        Wizard preNum = wizardList.get(2);
        if(preNum.isChecked()){

            desc.append("\n\n# Numbering will start from \'"+preNum.getParams().get(1)+
                    "\' and will be added  ");

                    if(preNum.getParams().get(0).toString().equals("*")){
                        desc.append("before file name");
                    }
                    else {
                        desc.append("before \'"+preNum.getParams().get(0)+"\'");
                    }
            desc.append(" in "+preNum.getParams().get(2)+" format.");
            //Log.d("Desc", desc.toString());
        }

        Wizard sufNum = wizardList.get(3);
        if(sufNum.isChecked()){
            desc.append("\n\n# Numbering will start from \'"+sufNum.getParams().get(1)+
                    "\' and will be added  ");
            if(sufNum.getParams().get(0).toString().equals("*")){
                desc.append("after file name");
            }
            else {
                desc.append("after \'"+sufNum.getParams().get(0)+"\'");
            }
            desc.append(" in "+sufNum.getParams().get(2)+" format.");
        }


        Wizard replace = wizardList.get(4);
        if(replace.isChecked()){
            desc.append("\n\n# Any Occurence of \'"+
                        replace.getParams().get(0)+
                        "\' in the file name will get replaced by "+replace.getParams().get(1)+".");
        }
        Wizard auc = wizardList.get(5);
        if(auc.isChecked()){
            desc.append("\n\n# All letters of the file name will be changed into uppercase.");
        }

        Wizard alc = wizardList.get(6);
        if(alc.isChecked()){
            desc.append("\n\n# All letter of the file name will be changed into lowercase.");
        }

        Wizard flc = wizardList.get(7);
        if(flc.isChecked()){
            desc.append("\n\n# First letter of each word in the file name will be changed into uppercase.");
        }

        Wizard remove = wizardList.get(8);
        if(remove.isChecked()){
            desc.append("\n\n# All characters between \'"+remove.getParams().get(0)+
                            "\' and \'"+remove.getParams().get(1)+
                            "\' will be erased.");
        }
        if(desc.toString().equals(""))
            return "\n\nEmpty Configuration";
        return desc.toString();
    }


}
