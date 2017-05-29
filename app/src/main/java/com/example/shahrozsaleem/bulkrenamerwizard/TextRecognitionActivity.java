package com.example.shahrozsaleem.bulkrenamerwizard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import java.net.URISyntaxException;

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

                File f = null;
                try {
                    f = new File(PathUtil.getPath(this, data.getData()));
                    InputStream is = new FileInputStream(f);
                    bitmap  = BitmapFactory.decodeStream(is);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


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


}

class PathUtil {
    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
