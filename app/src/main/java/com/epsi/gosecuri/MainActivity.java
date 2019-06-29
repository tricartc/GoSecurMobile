package com.epsi.gosecuri;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView sampleTextView;
    Button buttonChange;
    ImageView contentImageView;
    Button changeImageButton;
    Button takePhotoButton;
    Button saveDataButton;
    Button getDataCardButton;


    static int REQUEST_CODE=1664;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sampleTextView = findViewById(R.id.sampleTextView);
        buttonChange = findViewById(R.id.buttonChange);
        contentImageView = findViewById(R.id.contentImageView);
        changeImageButton= findViewById(R.id.changeImageButton);
        takePhotoButton= findViewById(R.id.takePhotoButton);
        saveDataButton= findViewById(R.id.saveDataButton);
        getDataCardButton= findViewById(R.id.getDataCardButton);

        /*buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sampleTextView.equals("Jerem est une pute"){
                    sampleTextView.setText("Jerem est une grosse pute");
                }else{
                    sampleTextView.setText("Jerem est une pute");
                }
            }
        });*/
    }

    public void changeActionButton(View changeButton) {
        if(sampleTextView.getText() == "Jerem est une pute"){
            sampleTextView.setText("Jerem est une grosse pute");
        }else{
            sampleTextView.setText("Jerem est une pute");
        }
    }

    public void onChangePhotoButton(View changePhotoButton){
        contentImageView.setImageResource(R.drawable.test_marmotte);
    }

    public void takePhoto(View photoButton) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(photoIntent, REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            Bitmap imageBitmap = (Bitmap)data.getExtras().get("data");
            contentImageView.setImageBitmap(imageBitmap);
        }
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            int targetW = contentImageView.getWidth();
            int targetH = contentImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            contentImageView.setImageBitmap(bitmap);
        }
    }

    public void saveData(View saveDataButton){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.push().setValue("Hello, World!");
        myRef.push().setValue(new Personne("Mister", "jo"));
        //sampleTextView.setText("coucou");
    }

    public void getDataFromIdCard(View getDataCardButton){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(((BitmapDrawable)getDrawable(R.drawable.carte_identite)).getBitmap());
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
                //.getCloudTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        for(FirebaseVisionText.TextBlock textBlock : firebaseVisionText.getTextBlocks()){
                            for(FirebaseVisionText.Line line : textBlock.getLines()){
                                Log.d("CTR", line.getText());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("CTR", "It's don't working man !! ");
                    }
                });
    }


    String currentPhotoPath;

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

}
