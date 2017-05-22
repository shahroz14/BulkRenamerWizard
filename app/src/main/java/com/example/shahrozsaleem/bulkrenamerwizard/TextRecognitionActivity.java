package com.example.shahrozsaleem.bulkrenamerwizard;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class TextRecognitionActivity extends AppCompatActivity {

    Button btn;
    ImageView image;
    Bitmap bitmap;
    Button saveBtn;
    Button copyToClipboard;
    TextView textView;
    TextView selectedImageTV;
    File textFilePath;
    StringBuilder result;
    ProgressDialog mProgress;
    TextRecognizer tr;

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
                tr = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!tr.isOperational()) {
                    Toast.makeText(getApplicationContext(), "Error! Update Google Play Services", Toast.LENGTH_SHORT).show();
                }
                else {

                    mProgress = new ProgressDialog(TextRecognitionActivity.this);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgress.setTitle("Processing");
                    mProgress.setMessage("Generating Text, Please Wait...");
                    mProgress.setCancelable(false);
                    mProgress.show();

                    TextGenerator textGenerator = new TextGenerator();
                    textGenerator.execute();


                }
            }
        });

    }

    class TextGenerator extends AsyncTask<String, String, String>{


        @Override
        protected String doInBackground(String... strings) {
            Log.d("Debug", bitmap.toString());
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = tr.detect(frame);
            result = new StringBuilder();

            for(int i=0; i<items.size(); i++){
                TextBlock item = items.valueAt(i);
                result.append(item.getValue()+"\n");
            }
            mProgress.cancel();
            publishProgress();
            return "DONE";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TextRecognitionActivity.this);
            final View dialog = TextRecognitionActivity.this.getLayoutInflater().inflate(R.layout.save_text_file, null);
            mBuilder.setView(dialog);
            textView = (TextView) dialog.findViewById(R.id.textGenTV);
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

            copyToClipboard = (Button) dialog.findViewById(R.id.copyToClipboard);
            copyToClipboard.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {

                    String stringYouExtracted = textView.getText().toString();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(stringYouExtracted);
                    Toast.makeText(TextRecognitionActivity.this, "copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });

            final AlertDialog alertDialog = mBuilder.create();
            alertDialog.show();
        }
    }

    public void selectImage(View v){

        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri data = Uri.parse(imageDir.getPath());
        imagePickerIntent.setDataAndType(data, "image/*");
        imagePickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(imagePickerIntent, 20);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==20){
            if(resultCode==RESULT_OK){
                File f = getBitmapFile(data);
                try {
                    InputStream is = new FileInputStream(f.getAbsolutePath());
                    /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = calculateInSampleSize(options, 300, 400);*/
                    bitmap  = BitmapFactory.decodeStream(is);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //bitmap = BitmapFactory.decodeFile(outputFileDir.getPath());

                String result = f.getName();
                selectedImageTV = (TextView)findViewById(R.id.selectedImageTV);
                selectedImageTV.setText(result);
                image.setImageBitmap(bitmap);
            }
            else{
                return;
            }
        }
    }

    public File getBitmapFile(Intent data) {
        Uri selectedImage = data.getData();
        Cursor cursor = getContentResolver().query(selectedImage, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
        cursor.moveToFirst();

        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String selectedImagePath = cursor.getString(idx);
        cursor.close();

        return new File(selectedImagePath);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
