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

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity {

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

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            final Context context = getApplicationContext();
            Marker marker;
            String addressLine;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            @Override
            public void onMapLongClick(LatLng point) {
                //TODO: Clean up code
                final LatLng temp = point;
                try {
                    addressList = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    addressLine = addressList.get(0).getAddressLine(0);
                    Log.w("test: ", addressList.get(0).getAddressLine(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Set marker on wherever the user long presses
                //TODO: Edit addMarker to accept user inputted strings from the dialog box when implemented

                //Choice given for the standard google maps marker or a custom marker
                AlertDialog alert = new AlertDialog.Builder(MapsActivity.this).create();
                alert.setMessage("Standard marker or custom marker?");
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Standard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.addMarker(new MarkerOptions().position(temp)
                                .title(addressLine)
                                .visible(true)
                                .snippet("Click to remove"));

                        //Feedback on placing marker
                        Toast toast = Toast.makeText(context, "Marker Placed!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Custom", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.addMarker(new MarkerOptions().position(temp)
                                .title(addressLine)
                                .visible(true)
                                .snippet("Click to remove")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.down_arrow)));

                        //Feedback on placing marker
                        Toast toast = Toast.makeText(context, "Marker Placed!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                alert.show();


                //Sets the snippet of the marker to remove if user presses it
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    //TODO: Dialog box for user to choose between deleting, editing or not doing anything when they click on info window, some kind of option
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        marker.remove();
                    }
                });
            }

        });


        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    private void addDrawer(){
        String[] testArray = { "Normal", "Satellite", "Hybrid", "Terrain" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
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
            }

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
