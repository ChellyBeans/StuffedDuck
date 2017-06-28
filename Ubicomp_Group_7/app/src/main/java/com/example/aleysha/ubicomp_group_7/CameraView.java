package com.example.aleysha.ubicomp_group_7;

import android.content.Context;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;

///
/// The following code was taken from:
/// [Tutorial] How to use camera with Android and Android Studio, by Aron Bordin
/// http://blog.rhesoft.com/2015/04/02/tutorial-how-to-use-camera-with-android-and-android-studio/
///
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean safeToTakePicture = false;

    public CameraView(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

        try{
            mCamera.setPreviewDisplay(mHolder);
        }
        catch(Exception e){}

        class PhotoTimer extends TimerTask
        {
            public void run()
            {
                try {
                    if (mHolder.getSurface() == null){ //check if the surface is ready to receive camera data
                        return;
                    }

                    try {
                        mCamera.stopPreview();
                    } catch (Exception e) {}

                    mCamera.startPreview();

                    safeToTakePicture = true;
                    if (safeToTakePicture) {
                        mCamera.takePicture(null, null, mPicture);
                        safeToTakePicture = false;
                    }


                } catch (Exception e) {}
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new PhotoTimer(), 0, 60000*Integer.parseInt(MainActivity.cameraFreq.getText().toString()));

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                safeToTakePicture = true;

            } catch (FileNotFoundException e) {

            } catch (IOException e) {}
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        MainActivity.UnsentImageAddresses.add(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GPSCoordindates");

        if(!documentStorageDir.exists())
        {
            if(!documentStorageDir.mkdirs())
            {
                return null;
            }
        }
        try {
            File gpsFile = new File(documentStorageDir, "GPS_" + timeStamp + ".txt");
            String gpsContent = "";
            for (int i = 0; i < MainActivity.UnsavedGPSLinks.size(); i++) {
                gpsContent += MainActivity.UnsavedGPSLinks.get(i) + "\n";
            }

            FileWriter filewriter = new FileWriter(gpsFile);
            filewriter.append(gpsContent);
            filewriter.flush();
            filewriter.close();

            MainActivity.UnsavedGPSLinks.clear();
        }
        catch (Exception e)
        {

        }

        if(MainActivity.alertModeCheckBox.isChecked())
            MainActivity.sendGmail();

        return mediaFile;
    }
}



