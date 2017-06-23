package com.example.aleysha.ubicomp_group_7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Camera;
import android.widget.FrameLayout;
import android.widget.Toast;

/// This activity is the base of the application. It is the only activity for the application
/// as the user will not be able to navigate the application due to the application running
/// inside a stuffed duck.
public class MainActivity extends AppCompatActivity {

    //widgets in the UI
    private Button button, button2, sendButton,camerabutton;
    private TextView textView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Camera camera = null;
    private CameraView cameraView = null;
    private int cameraId;

    static String loc ="";
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain UI elements here
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        camerabutton = (Button) findViewById(R.id.camerabutton);
        textView = (TextView) findViewById(R.id.textView);

        sendButton = (Button) findViewById(R.id.button3);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            GMailSender sender = new GMailSender(
                                    "ubicomp.duck@gmail.com",
                                    "duck@123");
                            /*
                            TODO: actual image path to be added
                             */
                            sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/627742_workout.jpg");
                            Log.v("MainActivity",Environment.getExternalStorageDirectory().toString());

                            sender.sendMail("Coordinates + Images", "Email Coordinates are: " +loc,
                                    "ubicomp.duck@gmail.com",
                                    "ubicomp.parent@gmail.com");
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                        }
                    }
                }).start();
            }
        });

        //location manager and listener to get location from Android
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //get coordinates and print to UI
                String lat = "" +location.getLatitude();
                String lon = "" +location.getLongitude();

                textView.append("\n " + lat + ", " + lon);
                /*
                TODO: Add location coordinates
                 */
                loc = loc + "\n 1" + lat + ", " + lon;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //go to Location settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        //TODO: make it so the application exits if important permissions are not fulfilled
        //Check for location permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                },10);
            }
        }
        else{
                //set up the buttons that need this access
                configureButton();
                configureCamera();
        }


    }

    //if location permissions are granted, start button listeners
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureButton();
                    configureCamera();
                }
        }
    }

    //start location listener
    public void configureButton(){
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //every 5 seconds take the location coordinates
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        });
    }

    // configure the camerabutton to open the camera upon click
    // if camera is available, will add the view to the framelayout camera_view
    public void configureCamera(){
        camerabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try{
                    camera = Camera.open(findBackFacingCamera());
                } catch (Exception e){}

                if(camera != null) {
                    cameraView = new CameraView(view.getContext(), camera);
                    FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
                    camera_view.addView(cameraView);
                }
            }
        });
    }

    //stop location listener
    public void stopLocation(View view){
        locationManager.removeUpdates(locationListener);
    }

    // Finds the rear facing camera and sends back the ID associated with it
    private int findBackFacingCamera() {

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
}
