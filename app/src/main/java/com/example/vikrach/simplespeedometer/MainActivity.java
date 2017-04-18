package com.example.vikrach.simplespeedometer;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.example.vikrach.simplespeedometer.R.id.mphkmh;
import static com.example.vikrach.simplespeedometer.R.id.mphkmh2;

public class MainActivity extends AppCompatActivity implements LocationListener {

    static DownloadManager dm;

    TextView speed;
    TextView mphKmh;
    TextView mphKmh2;
    TextView speedLimit;
    TextView lastUpdated;

    boolean kmh;
    boolean vibe;
    boolean ring;
    int alertSpeed;
    boolean permissionsOK = false;

    int mphLimit;
    int kmhLimit;

    long starttime;
    long currenttime;
    long starttime2;

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kmhLimit = (int) Math.round(mphLimit * 1.60934);

        speed = (TextView) findViewById(R.id.yourSpeed);
        mphKmh = (TextView) findViewById(mphkmh);
        mphKmh2 = (TextView) findViewById(mphkmh2);
        speedLimit = (TextView) findViewById(R.id.speedLimitText);
        lastUpdated = (TextView) findViewById(R.id.lastUpdatedTime);

        starttime = System.currentTimeMillis();
        starttime = System.currentTimeMillis();


        if (Build.VERSION.SDK_INT < 23) {
            //Do not need to check the permission
        } else {
            if (checkAndRequestPermissions()) {
                //If you have already permitted the permission
            }
        }


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        kmh = settings.getBoolean("check_box", false);
        vibe = settings.getBoolean("vibrate", true);
        ring = settings.getBoolean("ring", true);

        if (kmh) {

            speedLimit.setText(String.valueOf(kmhLimit));
            mphKmh.setText("km/h");
            mphKmh2.setText("km/h");
        } else {
            speedLimit.setText(String.valueOf(mphLimit));
            mphKmh.setText("mph");
            mphKmh2.setText("mph");
        }

        if(permissionsOK) {
            LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            this.onLocationChanged(null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            return;
        }


        String url = "http://www.overpass-api.de/api/xapi?*[bbox=";

        double latD = location.getLatitude();
        double lonD = location.getLongitude();

        String lat = String.valueOf(latD);
        String lon = String.valueOf(lonD);

        String lat2 = String.valueOf(latD + .001);
        String lon2 = String.valueOf(lonD + .001);

        url = url + lon + "," + lat + "," + lon2 + "," + lat2 + "]" + "[maxspeed=*]";



        currenttime = System.currentTimeMillis();
        if(currenttime-starttime2>5000)
        {
            Date d=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");
            String currentDateTimeString = sdf.format(d);
            lastUpdated.setText(currentDateTimeString);

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));
        dm.enqueue(request);


            class MyDownloadReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Uri uri = dm.getUriForDownloadedFile(id);


                String raw = URItoRaw(uri);
                String maxspeed = "?";

                try {
                    maxspeed = parseXML(raw);
                } catch (Exception ex) {
                    Toast.makeText(context, "ERROR", Toast.LENGTH_LONG).show();
                }


                if (!maxspeed.equals("?")) {
                    maxspeed = maxspeed.substring(0, (maxspeed.length() - 4));
                    mphLimit = Integer.valueOf(maxspeed);
                }


                File f = new File(uri.getPath());

                if(f.exists())
                f.delete();


                starttime2 = System.currentTimeMillis();
                if (kmh) {
                    kmhLimit = (int) Math.round(mphLimit * 1.60934);
                    speedLimit.setText(String.valueOf(kmhLimit));
                    mphKmh.setText("km/h");
                    mphKmh2.setText("km/h");
                } else {
                    speedLimit.setText(String.valueOf(mphLimit));
                    mphKmh.setText("mph");
                    mphKmh2.setText("mph");
                }

            }
        }
            registerReceiver(new MyDownloadReceiver(), new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }





        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        int nCurrentSpeed;

        if (location == null) {
            speed.setText("---");
        }
        else
        {
            if(kmh) {
                nCurrentSpeed = Math.round(location.getSpeed() * 3.6f);
                speed.setText(String.valueOf(nCurrentSpeed));
            }
            else {
                nCurrentSpeed = Math.round(location.getSpeed() * 2.23694f);
                speed.setText(String.valueOf(nCurrentSpeed));
            }

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

            if(kmh) {
                if (nCurrentSpeed > kmhLimit)
                {
                    currenttime = System.currentTimeMillis();
                    if(vibe)
                 v.vibrate(500);
                 try {
                      if (ring&&(currenttime-starttime>5000)) {
                        r.play();
                        starttime = System.currentTimeMillis();
                       }
                 }
                 catch (Exception e)
                   {
                        e.printStackTrace();
                   }
              } else {
                if (r.isPlaying()) {
                    r.stop();
                }
            }
            }
            else {
                if (nCurrentSpeed > mphLimit)
                {
                    currenttime = System.currentTimeMillis();
                    if(vibe)
                        v.vibrate(500);
                    try {
                        if (ring&&(currenttime-starttime>5000)) {
                            r.play();
                            starttime = System.currentTimeMillis();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } else {
                    if (r.isPlaying()) {
                        r.stop();
                    }
                }
            }
        }


    }

    public String parseXML(String raw) throws Exception
    {
        String max = "?";
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(raw)));

        NodeList tags = doc.getElementsByTagName("tag");


        for(int i = 0; i < tags.getLength(); i++)
        {
            final Element element = (Element) tags.item(i);


            if(element.getAttribute("k").equals("maxspeed"))
            {
                final String maxspeed = "" + element.getAttribute("v");
                max = maxspeed;
            }

        }

        return max;
    }


    public String URItoRaw(Uri uri) {
        String raw = "";

        try {
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            raw = sb.toString();
        } catch (IOException ex) {
            return "";
        }

        return raw;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(MainActivity.this, Preferences.class);
                startActivity(i);
                break;

        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onResume() {

        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        vibe = prefs.getBoolean("vibrate", true);
        ring = prefs.getBoolean("ring", true);
        kmh = prefs.getBoolean("check_box", false);
        alertSpeed = Integer.valueOf(prefs.getString("set_speed_preference", "5"));

        if(alertSpeed > 20) {
            prefs.
        }


        if (kmh) {
            kmhLimit = (int) Math.round(mphLimit * 1.60934);
            speedLimit.setText(String.valueOf(kmhLimit));
            mphKmh.setText("km/h");
            mphKmh2.setText("km/h");
        } else {
            speedLimit.setText(String.valueOf(mphLimit));
            mphKmh.setText("mph");
            mphKmh2.setText("mph");
        }


    }


    private boolean checkAndRequestPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);



        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission Granted Successfully.
                    permissionsOK = true;
                } else {
                    //You did not accept the request can not use the functionality.
                    permissionsOK = false;
                }
                break;
        }
    }

}
