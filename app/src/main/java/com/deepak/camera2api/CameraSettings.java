package com.deepak.camera2api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraSettings extends AppCompatActivity {

    public static String TAG = "CAMERA_SETTINGS";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static CameraManager manager;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraDevice cameraDevice;
    Button btnPreviewSettings, btnFlashSettings, btnAppCamera, btnInbuiltCamera;
    EditText etPreviewSetting, etFlashSetting;
    RadioGroup rgCamera;
    RadioButton rbFrontCamera, rbBackCamera;
    ImageView imageView;
    private Size imageDimension = null;
    private ArrayList<Size> imageDimentionArrayList = new ArrayList<>();
    private static String cameraId = "0";
    CameraCharacteristics characteristics = null;
    boolean frontFlash = false, backFlash = false, isFlashAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);
        btnPreviewSettings = findViewById(R.id.btnPreviewSettings);
        btnFlashSettings = findViewById(R.id.btnFlashSettings);
        btnAppCamera = findViewById(R.id.btnAppCamera);
        btnInbuiltCamera = findViewById(R.id.btnInbuiltCamera);
        imageView = findViewById(R.id.ivCapturedImage);
        etPreviewSetting = findViewById(R.id.etPreviewSetting);
        etFlashSetting = findViewById(R.id.etFlashSetting);

        rgCamera = findViewById(R.id.rgCamera);
        rbFrontCamera = findViewById(R.id.rbFrontCamera);
        rbBackCamera = findViewById(R.id.rbBackCamera);

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraPreviewSetting();

        rgCamera.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                imageDimension = null;
                etPreviewSetting.setText("");
                etFlashSetting.setText("");
                btnPreviewSettings.setText("Preview Settings");
                btnFlashSettings.setText("Is Flash Available");

                if (rbFrontCamera.isChecked()) {
                    Log.e(TAG, "Front Camera selected");
                    cameraId = "1";
                    //setupCamera();
                    cameraPreviewSetting();
                    isFlashAvailable = flashSetting();
                } else {
                    Log.e(TAG, "Back Camera selected");
                    cameraId = "0";
                    //setupCamera();
                    cameraPreviewSetting();
                    isFlashAvailable = flashSetting();
                }
            }
        });

        btnAppCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraSettings.this, Camera2Activity.class);
                if (imageDimension != null) {
                    intent.putExtra("resolution", imageDimension.toString());
                } else {
                    Toast.makeText(CameraSettings.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("isFlashAvailable", isFlashAvailable);
                if (cameraId.equals(null)) {
                    Toast.makeText(CameraSettings.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("cameraId", cameraId);
                startActivity(intent);
            }
        });

        btnPreviewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dimensionString[] = new String[0];
                StringBuffer dimenStringBuffer = new StringBuffer();
                if (imageDimentionArrayList.size() > 0) {
                    dimensionString = new String[imageDimentionArrayList.size()];
                    for (int i = 0; i < imageDimentionArrayList.size(); i++) {
                        dimensionString[i] = String.valueOf(imageDimentionArrayList.get(i));
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettings.this);
                    builder.setTitle("Make your selection");
                    builder.setItems(dimensionString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Do something with the selection
                            imageDimension = imageDimentionArrayList.get(item);
                            etPreviewSetting.setText(imageDimension.toString());
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        btnFlashSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashAvailable) etFlashSetting.setText("Flash Available");
                else etFlashSetting.setText("Flash Not Available");

            }
        });

        btnInbuiltCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }*/
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "dispatchTakePictureIntent: error while creating file");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.deepak.camera2api.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public StreamConfigurationMap setupCamera() {
        StreamConfigurationMap map = null;
        try {
            characteristics = manager.getCameraCharacteristics(cameraId);
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void cameraPreviewSetting() {
        try {
            StreamConfigurationMap map = setupCamera();
            assert map != null;
            //manager.setTorchMode(cameraId, true);
            int size = 0;
            size = map.getOutputSizes(SurfaceTexture.class).length;
            for (int i = 0; i < size; i++) {
                imageDimentionArrayList.add(map.getOutputSizes(SurfaceTexture.class)[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean flashSetting() {
        if (rbFrontCamera.isChecked()) {
            isFlashAvailable = characteristics.get(characteristics.FLASH_INFO_AVAILABLE);
        } else {
            isFlashAvailable = characteristics.get(characteristics.FLASH_INFO_AVAILABLE);
        }
        return isFlashAvailable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Glide.with(this).load(currentPhotoPath).into(imageView);
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);*/
        } else if(resultCode == RESULT_CANCELED) {
            // User Cancelled the action
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

}
