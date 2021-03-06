package adamlieu.simplemap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void goToSimpleMaps(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void goToHeatMap(View view){
        Intent intent = new Intent(this, MapsHeat.class);
        startActivity(intent);
    }

    public void goToMarkerCluster(View view){
        Intent intent = new Intent (this, MarkerCluster.class);
        startActivity(intent);
    }

    public void goToCanvasMap(View view){
        Intent intent = new Intent(this, CanvasMap.class);
        startActivity(intent);
    }

    public void goToCanvasView(View view){
        Intent intent = new Intent(this, CanvasView.class);
        startActivity(intent);
    }

    public void goToWeather(View view){
        Intent intent = new Intent(this, WeatherAnim.class);
        startActivity(intent);
    }
    public void goToCanvasMapWithAnim(View view){
        Intent intent = new Intent(this, CanvasMapWithAnim.class);
        startActivity(intent);
    }
}
