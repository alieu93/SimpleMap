package adamlieu.simplemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class MapsHeat extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    ArrayList<LatLng> bikes = new ArrayList<LatLng>();

    GroundOverlay imageOverlay;

    LocationManager locManager;// = (LocationManager)getSystemService(LOCATION_SERVICE);
    Location location;
    String provider;
    Criteria criteria = new Criteria(); // Criteria object to get provider

    boolean heatmap = false;

    //For drawer
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)

    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;


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
        setContentView(R.layout.activity_maps_heat);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);


        try {
            bikes = readGPS();
        } catch (JSONException e){
            Toast.makeText(this, "Error with reading JSON file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("JSONException", e.toString());
        }

        LatLng test = new LatLng(0,0);

        for(LatLng i : bikes){
            Log.v("Lat/Lng", "" + i.latitude + " , " + i.longitude);
            //mMap.addMarker(new MarkerOptions().position(i).visible(true));
        }

        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        //Heatmap
        /*
        mProvider = new HeatmapTileProvider.Builder().data(bikes).build();
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        //mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        mOverlay.clearTileCache();
        */

    }

    private void addDrawer(){
        String[] testArray = { "Heatmap", "Overlay"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0:
                    if(imageOverlay != null)
                        imageOverlay.remove();
                    mProvider = new HeatmapTileProvider.Builder().data(bikes).build();
                    mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    //mProvider.setGradient(ALT_HEATMAP_GRADIENT);
                    mOverlay.clearTileCache();
                    heatmap = true;
                    break;
                case 1:
                    if(heatmap) {
                        mOverlay.remove();
                    }
                    for(LatLng i : bikes){
                        GroundOverlayOptions icon = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.bike))
                                .position(i, 100f, 100f)
                                .transparency(0.80f);
                        imageOverlay = mMap.addGroundOverlay(icon);
                    }
            }
        }
    }

        @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private ArrayList<LatLng> readGPS() throws JSONException {
        ArrayList<LatLng> readGPS = new ArrayList<LatLng>();
        String test = loadJSONFromLocal();
        JSONObject object = new JSONObject(test);
        JSONArray array = object.getJSONArray("stationBeanList");

        Log.v("Array Length", "" + array.length());
        for(int i = 0; i < array.length(); i++){
            JSONObject temp = array.getJSONObject(i);
            int id = temp.getInt("id");
            String name = temp.getString("stationName");
            int docks = temp.getInt("totalDocks");
            double lat = temp.getDouble("latitude");
            Log.v("Lat", "" + lat);
            double lng = temp.getDouble("longitude");
            readGPS.add(new LatLng(lat, lng));
        }
        return readGPS;
    }

    public String loadJSONFromLocal(){
        String json = null;
        try{
            InputStream input = getResources().openRawResource(R.raw.bikestations);
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
            Log.i("Log JSON", json);

            return json;
        } catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
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
            //LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng toronto = new LatLng(43.7000, -79.4000);
            //Add a marker on the map with the current position
            //mMap.addMarker(new MarkerOptions().position(toronto).title("Current Position"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(toronto, 13);
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps_heat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
