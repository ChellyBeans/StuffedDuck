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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.hardware.Camera;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/// This activity is the base of the application. It is the only activity for the application
/// as the user will not be able to navigate the application due to the application running
/// inside a stuffed duck.
public class MainActivity extends AppCompatActivity {

    //widgets in the UI
    public static Button start;
    public static EditText gpsFreq, cameraFreq, deviceEmail, devicePassword, parentEmail;
    public static CheckBox alertModeCheckBox, cameraModeCheckBox;
    public static TextView gpsView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Camera camera = null;
    private CameraView cameraView = null;
    private int cameraId;
    public static Boolean alertMode = true;

    // Google map links of GPS coords
    public static HashMap<String, String> UnsavedGPSLinks = new HashMap<String, String>();
    public static ArrayList<String> UnsentImageAddresses = new ArrayList<String>();
    public static LinkedList<TimeCoordPair>  UnsentGPSLinks = new LinkedList<>();

    static String loc ="";
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain UI elements here
        start = (Button) findViewById(R.id.StartBtn);
        gpsFreq = (EditText) findViewById(R.id.GPSFreqEntry);
        cameraFreq = (EditText) findViewById(R.id.CameraFreqEntry);
        deviceEmail = (EditText) findViewById(R.id.DeviceEmailEntry);
        devicePassword = (EditText) findViewById(R.id.DevicePasswordEntry);
        parentEmail = (EditText) findViewById(R.id.ParentEmailEntry);
        gpsView = (TextView) findViewById(R.id.GPSView);
        alertModeCheckBox = (CheckBox) findViewById(R.id.AlertModeCheckBox);
        cameraModeCheckBox = (CheckBox) findViewById(R.id.CameraModeCheckBox);

        //location manager and listener to get location from Android
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String timeStamp = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss")
                        .format(new Date());

                //get coordinates and print to UI
                String lat = "" +location.getLatitude();
                String lon = "" +location.getLongitude();

                gpsView.append("\n " + lat + ", " + lon);
                
                loc = loc + "\n 1" + lat + ", " + lon;
                UnsavedGPSLinks.put(timeStamp, "http://maps.google.com/maps?q="+ lat + "," + lon);
                //UnsentGPSLinks.put(timeStamp, "http://maps.google.com/maps?q="+ lat + "," + lon);
                UnsentGPSLinks.add(new TimeCoordPair(timeStamp, new Coordinates(lon, lat)));
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
                configureStart();
        }


    }

    //if location permissions are granted, start button listeners
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureStart();
                }
        }
    }

    // configures the start button to set up GPS and Camera
    public void configureStart(){
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                configureGPS();
                configureCamera();
            }
        });
    }

    //start location listener
    public void configureGPS(){
        //every 5 seconds take the location coordinates
        locationManager.requestLocationUpdates("gps",
                (long)(60000*Double.parseDouble(gpsFreq.getText().toString()))
                , 0, locationListener);
    }

    // configure the camerabutton to open the camera upon click
    // if camera is available, will add the view to the framelayout camera_view
    public void configureCamera(){
        try{
            camera = Camera.open(findBackFacingCamera());
        } catch (Exception e){}

        if(camera != null) {
            cameraView = new CameraView(this.getApplicationContext(), camera);
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.CameraView);
            camera_view.addView(cameraView);
        }
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

    // sends an gmail to the parent with what information the device tracked
    public static void sendGmail()
    {
        new Thread(new Runnable() {
            public void run() {
                try {
                    GMailSender sender = new GMailSender(
                            deviceEmail.getText().toString(),
                            devicePassword.getText().toString());

                    if(cameraModeCheckBox.isChecked()) {
                        for (int i = 0; i < UnsentImageAddresses.size(); i++) {
                            sender.addAttachment(UnsentImageAddresses.get(i));
                        }
                    }


                    String bodyCoordinates = "";

                    if(UnsentGPSLinks.size() == 0)
                    {
                        bodyCoordinates += "No GPS addresses available during this notification.";
                    }
                    else {
                        bodyCoordinates += "Recorded GPS coordinates:\n";

                        int i = 0;
                        Boolean firstWayPoint = true;
                        String route = "https://www.google.com/maps/dir/?api=1";
                        Boolean getRoute = UnsentGPSLinks.size() >= 2;

                        for (TimeCoordPair pair: UnsentGPSLinks) {
                            String coord = pair.coordinates.latitude + "," + pair.coordinates.longitude;
                            bodyCoordinates += pair.time + "\t" + "http://maps.google.com/maps?q=" + coord + "\n";

                            // if we have enough points (at least two) we can create a route
                            if(getRoute) {
                                if (i == 0) //origin
                                    route += "&origin=" + coord;
                                else if (i == UnsentGPSLinks.size() - 1) //destination
                                    route += "&destination=" + coord;
                                else { //waypoints
                                    if (firstWayPoint) {
                                        route += "&waypoints=" + coord;
                                        firstWayPoint = false;
                                    } else {
                                        route += "|" + coord;
                                    }
                                }
                                i++;
                            }
                        }

                        if(getRoute) {
                            route += "&travelmode=walking\n";
                            bodyCoordinates += "\n Route (estimated):\n" + route;
                        }


                    }


                    Log.v("MainActivity",Environment.getExternalStorageDirectory().toString());

                    sender.sendMail("STUFFED NOTIFICATION: " + new Date().toString(), bodyCoordinates,
                            deviceEmail.getText().toString(),
                            parentEmail.getText().toString());

                    UnsentImageAddresses.clear();
                    UnsentGPSLinks.clear();

                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    public static void writeGPSFile()
    {
        File documentStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GPSCoordindates");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());

        if(!documentStorageDir.exists())
        {
            if(!documentStorageDir.mkdirs())
            {
                return;
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
    }
}

