package adamlieu.simplemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;

public class CanvasMap extends FragmentActivity {

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

    CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Context context = getApplicationContext(); //For Toast
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String gpsProvider = LocationManager.GPS_PROVIDER;

        //Prompts user to enable location services if it is not already enabled
        if (!locManager.isProviderEnabled(gpsProvider)) {

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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);


        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        Polyline line = mMap.addPolyline(new PolylineOptions().add(new LatLng(43.945791, -78.905847), new LatLng(43.943010, -78.889110)).width(5).color(Color.RED));

        int color = 0x7FF0F8FF;
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(43.945791, -78.894689))
                .radius(750)
                .strokeColor(0x7F000000)
                .fillColor(0x7FFFFFFF);

        Circle circle = mMap.addCircle(circleOptions);
        /*
        canvasView = (CanvasView) findViewById(R.id.CanvasView);
        Bitmap temp  = getBitmapFromView(canvasView.getBitmapFromView(R.id.CanvasView));
        */



    }

    private void addDrawer(){
        String[] testArray = { "Normal", "Satellite", "Hybrid", "Terrain" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
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

    public static Bitmap getBitmapFromView (CanvasView view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();
        if(background != null)
            background.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        view.draw(canvas);
        return bitmap;
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
            //LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng currentPos = new LatLng(43.945791, -78.894689);
            //Add a marker on the map with the current position
            mMap.addMarker(new MarkerOptions().position(currentPos).title("UOIT"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 15);
            mMap.animateCamera(cameraUpdate);
        }
    }
}