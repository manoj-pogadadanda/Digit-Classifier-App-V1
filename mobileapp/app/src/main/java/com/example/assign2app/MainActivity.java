package com.example.assign2app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button capture_image_btn = (Button)findViewById(R.id.capture_image_btn);
        Button exit_btn = (Button)findViewById(R.id.exit_btn);

        exit_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which){
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            //Yes button clicked
                                                            finish();
                                                            System.exit(0);
                                                            break;

                                                        case DialogInterface.BUTTON_NEGATIVE:
                                                            //No button clicked
                                                            break;
                                                    }
                                                }
                                            };

                                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                            builder.setMessage(R.string.exit_Message).setPositiveButton("Yes", dialogClickListener)
                                                    .setNegativeButton("No", dialogClickListener).show();
                                        }
                                    }
        );

        capture_image_btn.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {

                                                     DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialog, int which) {
                                                             switch (which){
                                                                 case DialogInterface.BUTTON_POSITIVE:
                                                                     //Yes button clicked
                                                                     if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                                             == PackageManager.PERMISSION_DENIED) {
                                                                         // Permission is not granted
                                                                         ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);
                                                                     }
                                                                     else {
                                                                         captureImage();
                                                                     }
                                                                     break;

                                                                 case DialogInterface.BUTTON_NEGATIVE:
                                                                     //No button clicked
                                                                     break;
                                                             }
                                                         }
                                                     };

                                                     AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                                     builder.setMessage(R.string.dialog_Message).setPositiveButton("Yes", dialogClickListener)
                                                             .setNegativeButton("No", dialogClickListener).show();
                                                 }
                                             }
        );

    }

    void captureImage() {
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File imageFile = null;
            try {
                SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
                String currentDate = s.format(new Date());
                imageFile = File.createTempFile("CapturedImage_" + currentDate + "_",  ".jpg",
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                imagePath = imageFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider2",
                        imageFile);
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(captureImageIntent, CAMERA_REQUEST_CODE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE)
        {
//            Intent selectCategoryIntent = new Intent(MainActivity.this, CategoryActivity.class);
//            selectCategoryIntent.putExtra("CapturedImage", imagePath);
//            startActivityForResult(selectCategoryIntent, CATEGORY_REQUEST_CODE);

            //Intent intent = getIntent();
            //String currentPhotoPath = intent.getExtras().getString("CapturedImage");
            File photoFile = new File(imagePath);
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                    "com.example.android.fileprovider2",
                    photoFile);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            Bitmap rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();


            //converting image to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            rotatedImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            String Url = getString(R.string.file_server);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Url, new Response.Listener<String>(){
                @Override
                public void onResponse(String s) {
                    if(s.equals("Success")){
                        Toast.makeText(MainActivity.this, photoFile.getName()
                                        + " Uploaded Successfully ",
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_LONG).show();
                    }
                }},
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(MainActivity.this, "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();;
                        }
                    }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(getString(R.string.image_key), imageString);
                    params.put("filename", photoFile.getName());
                    return params;
                }
            };
            requestQueue.add(stringRequest);

        }

    }

}