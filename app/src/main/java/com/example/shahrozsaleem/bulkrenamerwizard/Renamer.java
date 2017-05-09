package com.example.shahrozsaleem.bulkrenamerwizard;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Renamer {
	
	
	private File mainFolder;
	private ArrayList<File> files;
	private ArrayList<File> subFolders;
    private static File wizardFile;
    private static Context context;
    private Long preStartFrom = null;
    private Long sufStartFrom = null;


	public Renamer(String dirPath) {
        Log.d("Constructor", "Called");
		this.mainFolder = new File(dirPath);
		files = new ArrayList<File>();
		subFolders = new ArrayList<File>();
		
		// Checking whether the provided path is valid
		
		if(!(mainFolder.isDirectory())){
			System.out.println("Not a valid path.");
			return;
		}
		
		separateFilesAndFolders();
	}


	public Renamer(File root, ArrayList<File> folders, ArrayList<File> files, File wizardFile, Context context){
        Log.d("Constructor", "Called");
        this.mainFolder = root;
		this.files = files;
		this.subFolders = folders;
        this.wizardFile = wizardFile;
        //Toast.makeText(context, wizardFile.toString(), Toast.LENGTH_LONG).show();
        this.context = context;
	}



    void setMainFolder(File root){
        mainFolder = root;
    }
	
	void separateFilesAndFolders(){
		File[] folderContent = mainFolder.listFiles();
		for (File file : folderContent) {
			if(file.isFile()&&!file.isHidden())
				files.add(file);
			
			if(file.isDirectory()&&!file.isHidden())
				subFolders.add(file);
		}
	}
	
	
	
	/**
	 * renaming each file in the folder, 
	 * before that skipping hidden files and 
	 * ensuring sub-folders are discarded.
	**/
	
	void rename(){	
		for (File file : files) {
                String ext = getExtension(file.getName());
				String newName = getModifiedName(removeExtensionFromFile(file.getName()));
				Log.d("Debug", file.getParent()+" "+newName);
				file.renameTo(new File(file.getParent(), newName+"."+ext));
				//Log.d("DebugFilePath", mainFolder.getPath());
		}
		
		for (File file : subFolders) {
			new Renamer(file.getPath()).rename();
		}
	}

	String removeExtensionFromFile(String fileName){

        int lastIndex = fileName.lastIndexOf('.');
        if(lastIndex>=0)
            return fileName.substring(0, lastIndex);
        return fileName;

    }

    String getExtension(String fileName){
        int lastIndex = fileName.lastIndexOf('.');
        if(lastIndex>=0)
            return fileName.substring(lastIndex+1, fileName.length());
        return "";
    }
	
    String prefixNumberingInNumerics(String name){
        //Log.d("Debug", pre+"");
        return (preStartFrom++)+" "+name;
	}

	String prefixNumberinginAlpha(String name){
        long n = preStartFrom++;
        long r ;
        StringBuilder alpha = new StringBuilder();
        while(n>=0){
            r = n % 26;
            alpha.insert(0, (char)(r+97));
            n = (n/26);
            n--;
        }
        return alpha.toString()+" "+name;
    }

	String prefixNumberingInRoman(String name){
        String roman = getRoman((int) (long) preStartFrom++);
        if(roman.equals("Invalid Roman Number Value")) {
            Toast.makeText(context, "Roman Too Large", Toast.LENGTH_LONG).show();
            return name;
        }
        else{
            return roman+" "+name;
        }
    }
    
    static String getRoman(int num){
        if (num < 1 || num > 3999)
            return "Invalid Roman Number Value";
        String s = "";
        while (num >= 1000) {
            s += "M";
            num -= 1000;        }
        while (num >= 900) {
            s += "CM";
            num -= 900;
        }
        while (num >= 500) {
            s += "D";
            num -= 500;
        }
        while (num >= 400) {
            s += "CD";
            num -= 400;
        }
        while (num >= 100) {
            s += "C";
            num -= 100;
        }
        while (num >= 90) {
            s += "XC";
            num -= 90;
        }
        while (num >= 50) {
            s += "L";
            num -= 50;
        }
        while (num >= 40) {
            s += "XL";
            num -= 40;
        }
        while (num >= 10) {
            s += "X";
            num -= 10;
        }
        while (num >= 9) {
            s += "IX";
            num -= 9;
        }
        while (num >= 5) {
            s += "V";
            num -= 5;
        }
        while (num >= 4) {
            s += "IV";
            num -= 4;
        }
        while (num >= 1) {
            s += "I";
            num -= 1;
        }
        return s;
    }

	String suffixNumberingInNumerics(String name){
        return name+" "+(sufStartFrom++);
    }


    String suffixNumberinginAlpha(String name){
        long n = sufStartFrom++;
        long r ;
        StringBuilder alpha = new StringBuilder();
        while(n>=0){
            r = n % 26;
            alpha.insert(0, (char)(r+97));
            n = (n/26);
            n--;
        }
        return name+" "+alpha.toString();
    }


    String suffixNumberingInRoman(String name){
        String roman = getRoman((int)(long) sufStartFrom++);
        if(roman.equals("Invalid Roman Number Value")) {
            Toast.makeText(context, "Roman Too Large", Toast.LENGTH_LONG).show();
            return name;
        }
        else {
            return name + " " + roman;
        }
    }

	private String getModifiedName(String name){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(wizardFile);
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong with wizard file.", Toast.LENGTH_LONG).show();
        }


        //Add Prefix
        Wizard addPre = null;
        try {
            addPre = (Wizard) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

        }
        if(addPre.isChecked()){
            name = addPrefix(name, addPre.getParams().get(0), false);
        }


        //Add Suffix
        Wizard addSuff = null;
        try {
            addSuff = (Wizard) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(addSuff.isChecked()) {
            name = addSuffix(name, addSuff.getParams().get(0), false);
        }


        // Prefix Numbering
        Wizard preNum = null;
        try {
            preNum = (Wizard) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(preNum.isChecked()){
            String startFrom = preNum.getParams().get(0);
            String numBy = preNum.getParams().get(1);
            if(preStartFrom==null)
                preStartFrom = Long.parseLong(startFrom);

            if(numBy.equals("Numeric")){
                name = prefixNumberingInNumerics(name);
            }
            else if(numBy.equals("Alpha")){
                name = prefixNumberinginAlpha(name);
            }
            else if(numBy.equals("Roman")){
                name = prefixNumberingInRoman(name);
            }
        }


        //Suffix Numbering
        Wizard sufNum = null;
        try {
            sufNum = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(sufNum.isChecked()){
            String startFrom = sufNum.getParams().get(0);
            String numBy = sufNum.getParams().get(1);
            if(sufStartFrom==null)
                sufStartFrom = Long.parseLong(startFrom);

            if(numBy.equals("Numeric")){
                name = suffixNumberingInNumerics(name);
            }
            else if(numBy.equals("Alpha")){
                name = suffixNumberinginAlpha(name);
            }
            else if(numBy.equals("Roman")){
                name = suffixNumberingInRoman(name);
            }
        }


        //Replace Stirng
        Wizard replace = null;
        try {
            replace = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(replace.isChecked()){
            String replaceString = replace.getParams().get(0);
            String with = replace.getParams().get(1);
            name = replaceStringWith(name, replaceString, with);

        }


        //All Upper Case
        Wizard auc = null;
        try {
            auc = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(auc.isChecked()){
            name = allCapitalLetters(name);
        }


        //All Lower Case
        Wizard alc = null;
        try {
            alc = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(preNum.isChecked()){
            name = allSmallLetters(name);
        }


        //First Letter Capital
        Wizard flc = null;
        try {
            flc = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(flc.isChecked()){
            name = firstLetterCapital(name);
        }



        //Remove
        Wizard remove = null;
        try {
            remove = (Wizard) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(remove.isChecked()){
            String between = remove.getParams().get(0);
            String and = remove.getParams().get(1);
            name = remove(name, between, and);
        }

		return name.trim();
	}

	private static String remove(String name, String between, String and){
		/**
		 * deleting all characters inside parenthesis '( )', '[ ]'
		 * making pattern using regular expression and
		 * splitting the file name with string generated by regular expression.
		 */
        Pattern p = Pattern.compile(between+"?"+and);
        String[] words = p.split(name);
        name = "";
        for (String string : words)
            name += string;
        return name;
	}
	
	private static String startWithLetter(String name){
		Pattern p = Pattern.compile("[a-zA-Z].*");
		Matcher m = p.matcher(name);
		name = "";
		while(m.find())
			name = m.group();
		return name;
	}
	
	static String allCapitalLetters( String name){
		StringBuilder mName = new StringBuilder("");
		for( int i=0; i<name.length(); i++)
			mName.append( Character.toUpperCase(name.charAt(i)));
		return mName.toString();
	}
	
	static String allSmallLetters( String name){
		StringBuilder mName = new StringBuilder("");
		for( int i=0; i<name.length(); i++)
			mName.append( Character.toLowerCase(name.charAt(i)));
		return mName.toString();
	}
	
	private static String replaceStringWith(String name, String replace, String with){

        String[] occ = name.split(replace);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<occ.length-1; i++) {
            sb.append(occ[i]+with);
        }
        sb.append(occ[occ.length-1]);
        return sb.toString();

	}
	
	private static String addPrefix(String name, String prefix, boolean withSpace){
		if(withSpace)
			name = prefix+" "+name;
		else
			name = prefix+name;
		return name;
	}
	
	private static String addSuffix(String name, String suffix, boolean withSpace){
		if(withSpace)
			name = name+" "+suffix;
		else
			name = name+suffix;
		return name;
	}
	
	private static String firstLetterCapital(String name){
		if(name==null || name.equals(""))
			Log.d("Strinng", "Empty");
		String[] words = name.split(" ");
		name = "";
		for (int i = 0; i < words.length; i++) {
			char ch = words[i].charAt(0);
			words[i] = Character.toUpperCase(ch)+(words[i].substring(1)).toLowerCase();
			name += words[i]+" ";
		}
		return name;
	}


}


class Wizard implements Serializable{

	private boolean isChecked;
	private List<String> params;

	public Wizard(){
		isChecked = false;
		params = null;
	}

	public Wizard(List<String> params){
		isChecked = true;
		this.params = params;
	}

	List<String> getParams(){
        return params;
    }

    void setParams(List<String> params){
        this.params = params;
    }

    boolean isChecked(){
        return isChecked;
    }

    void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nChecked : "+isChecked);
        if(params!=null)
            sb.append("\nParameters : "+params.toString());
        return sb.toString();
    }
}
