package adamlieu.simplemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import static com.google.android.gms.maps.GoogleMap.*;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherAnim extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.


    LocationManager locManager;// = (LocationManager)getSystemService(LOCATION_SERVICE);
    Location location;
    String provider;
    Criteria criteria = new Criteria(); // Criteria object to get provider

    //Geocoder geocoder;
    List<Address> addressList;

    //For drawer
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    //ArrayList<LatLng> bikes = new ArrayList<LatLng>();
    ArrayList<String> weather = new ArrayList<String>();
    ArrayList<String> dayList = new ArrayList<String>();

    private ClusterManager<MyItem> mClusterManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Context context = getApplicationContext(); //For Toast
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String gpsProvider = LocationManager.GPS_PROVIDER;

        //Prompts user to enable location services if it is not already enabled
        if (!locManager.isProviderEnabled(gpsProvider)) {
            /*Toast toast =  Toast.makeText(context, "Location GPS must be enabled!", Toast.LENGTH_LONG);
            toast.show();*/

            //Alert Dialog
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Notice");
            alertDialog.setMessage("Location GPS must be enabled!");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String locConfig = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                    Intent enableGPS = new Intent(locConfig);
                    startActivity(enableGPS);
                }
            });
            alertDialog.show();
        }

        //mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        //Map Types
        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Default
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //Mix
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);



        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        try {
            weather = readWeather();
        } catch (JSONException e){
            Toast.makeText(this, "Error with reading JSON file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("JSONException", e.toString());
        }


        for(int i = 0; i < weather.size(); i++){
            Log.v("Weather array: ", weather.get(i));
        }

        for(int j = 0; j  < dayList.size(); j++){
            Log.v("Day array: ", dayList.get(j));
        }



    }

    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }

    private void addDrawer(){
        String[] testArray = { "Normal", "Satellite", "Hybrid", "Terrain", "Start Timer" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
        int i = 0;
        //TODO: For now, make options change the map type (satellite, normal, etc)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(context, "Working!", Toast.LENGTH_LONG).show();

            switch(position) {
                case 0:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 2:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 3:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case 4:
                    new CountDownTimer(20000, 1000){
                        public void onTick(long millisUntilFinished){
                            //Toast.makeText(context, "period: " + millisUntilFinished/1000, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(context, "period: " + i, Toast.LENGTH_SHORT).show();
                            switch(weather.get(i)) {
                                case "partlycloudy":
                                    Toast.makeText(context, i + dayList.get(i) + " cloudy", Toast.LENGTH_SHORT).show();
                                case "nt_clear":
                                    Toast.makeText(context, i + dayList.get(i) + " night clear", Toast.LENGTH_SHORT).show();
                                case "chancerain":
                                    Toast.makeText(context, i + dayList.get(i) + " chance rain", Toast.LENGTH_SHORT).show();
                                case "nt_chancerain":
                                    Toast.makeText(context, i + dayList.get(i) + " Night chance rain", Toast.LENGTH_SHORT).show();
                                case "nt_partlycloudy":
                                    Toast.makeText(context, i + dayList.get(i) + " Night partly cloudy", Toast.LENGTH_SHORT).show();
                                case "nt_rain":
                                    Toast.makeText(context, i + dayList.get(i) + " Night rain", Toast.LENGTH_SHORT).show();
                                case "rain:":
                                    Toast.makeText(context, i + dayList.get(i) + " Rain", Toast.LENGTH_SHORT).show();
                            }
                            i++;
                        }
                        public void onFinish(){
                            i = 0;
                            for(int j = 0; j < weather.size(); j++){
                                Log.v("Finished array: ", j + " " + dayList.get(i) + " " + weather.get(j));
                            }
                        }
                    }.start();
            }

        }
    }

    private ArrayList<String> readWeather() throws JSONException {
        //ArrayList<LatLng> readGPS = new ArrayList<LatLng>();
        ArrayList<String> weath = new ArrayList<String>();
        String test = loadJSONFromLocal();
        JSONObject object = new JSONObject(test);
        JSONArray array = object.getJSONArray("forecastday");
        //JSONArray array = object.getJSONObject("forecastday");

        Log.v("Array Length", "" + array.length());
        for(int i = 0; i < array.length(); i++){
            JSONObject temp = array.getJSONObject(i);
            int period = temp.getInt("period");
            String title = temp.getString("title");
            String forecast = temp.getString("icon");
            dayList.add(title);
            weath.add(forecast);

            Log.v("forecast", "" + period + " " + title + " " + forecast);

        }
        return weath;
    }

    public String loadJSONFromLocal(){
        String json = null;
        try{
            //InputStream input = getResources().openRawResource(R.raw.bikestations);
            InputStream input = getResources().openRawResource(R.raw.toronto);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            //input.close();

            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            json = sb.toString();

            //Only for testing
            /*
            String root = Environment.getExternalStorageDirectory().toString();
            Log.i("Output", root);
            File myDir = new File(root);
            File file = new File(myDir, "file.txt");
            try {
                FileOutputStream out = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(out);
                pw.println(json);
                pw.flush();
                pw.close();
                out.close();
            } catch (IOException e){
                e.printStackTrace();
            }*/
            //InputStream input = this.getAssets().open("bikestations.json");
            /*
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            json = new String(buffer, "UTF-8");*/
            //Log.i("Log JSON", json);

            return json;
        } catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        provider = locManager.getBestProvider(criteria, true); // Name for best provider
        //Check for permissions if they are granted
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
        location = locManager.getLastKnownLocation(provider); // Get last known location, basically current location
        if(location != null){
            //Get current long and lat positions
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            //Add a marker on the map with the current position
            mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 15);
            mMap.animateCamera(cameraUpdate);
        }
    }
}
