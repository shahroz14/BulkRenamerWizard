package com.example.shahrozsaleem.bulkrenamerwizard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class TextRecognitionActivity extends AppCompatActivity {

    Button btn;
    ImageView image;
    Bitmap bitmap;
    Button saveBtn;
    TextView textView;
    TextView selectedImageTV;
    File textFilePath;
    StringBuilder result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);
        getSupportActionBar().setTitle("Image Text Converter");
        textFilePath = new File(Environment.getExternalStorageDirectory().getPath()+"/"+getString(R.string.app_name)+"/text_files");
        if(!textFilePath.exists()){
            textFilePath.mkdirs();
        }
        Log.d("Debug", textFilePath.getPath());
        btn = (Button) findViewById(R.id.genBtn);
        image = (ImageView) findViewById(R.id.imgIV);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);
        image.setImageBitmap(bitmap);

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                TextRecognizer tr = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!tr.isOperational())
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = tr.detect(frame);
                    result = new StringBuilder();
                    for(int i=0; i<items.size(); i++){
                        TextBlock item = items.valueAt(i);
                        result.append(item.getValue()+"\n");
                    }
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TextRecognitionActivity.this);
                    final View dialog = TextRecognitionActivity.this.getLayoutInflater().inflate(R.layout.save_text_file, null);
                    mBuilder.setView(dialog);
                    textView = (TextView) dialog.findViewById(R.id.wizardDescTV);
                    textView.setText(result.toString());
                    saveBtn = (Button) dialog.findViewById(R.id.saveBtn);
                    saveBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            FileWriter fw= null;
                            try {
                                if(selectedImageTV==null){
                                    Toast.makeText(TextRecognitionActivity.this, "Can't save this file", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String fileName = selectedImageTV.getText().toString();
                                File textFile = new File(textFilePath.getPath(), Renamer.removeExtensionFromFile(fileName)+".txt");
                                if(!textFile.exists())
                                    textFile.createNewFile();
                                fw = new FileWriter(textFile);
                                fw.write(result.toString());
                                fw.flush();
                                fw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(TextRecognitionActivity.this, "File saved in "+textFilePath.getPath(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    final AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();

                }
            }
        });

    }

    public void selectImage(View v){

        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri data = Uri.parse(imageDir.getPath());
        imagePickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(imagePickerIntent, 20);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==20){
            Uri outputFileDir = data.getData();
            String result = getImageNameFromUri(outputFileDir);
            selectedImageTV = (TextView)findViewById(R.id.selectedImageTV);
            selectedImageTV.setText(result);
            bitmap = BitmapFactory.decodeFile(outputFileDir.getPath());
            image.setImageBitmap(bitmap);
        }


    }


    static String getImageNameFromUri(Uri imageUri){
        String[] nameSplit = imageUri.toString().split("/");
        return nameSplit[nameSplit.length-1];
    }

}
