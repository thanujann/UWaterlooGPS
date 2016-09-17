package lab1_202_03.uwaterloo.ca.lab4_202_03;

import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ca.uwaterloo.sensortoy.mapper.InterceptPoint;
import ca.uwaterloo.sensortoy.mapper.MapLoader;
import ca.uwaterloo.sensortoy.mapper.MapView;
import ca.uwaterloo.sensortoy.mapper.NavigationalMap;
import ca.uwaterloo.sensortoy.LineGraphView;
import ca.uwaterloo.sensortoy.mapper.PositionListener;

public class MainActivity extends AppCompatActivity{
    // For data extraction only
    File root = android.os.Environment.getExternalStorageDirectory();
    File dir = new File (root.getAbsolutePath() + "/download");
    File file = new File(dir, "Output.csv");

    // Instantiate LineGraphView object
    LineGraphView accelGraph;
    // LineGraphView magFieldGraph;
    static MapView mv;
    static NavigationalMap map;

    ArrayList<PointF> pathPoints = new ArrayList<PointF>();
    static ArrayList<PointF> directionPoints = new ArrayList<PointF>();

    // Instantiate global listeners for resetting purposes
    AccelerometerEventListener accelListener;
    MagneticFieldSensorEventListener magFieldListener;

    static PointF initialUserPoint = new PointF(0,0);
    static PointF destPoint = new PointF(0,0);
    static boolean PathCreated = false;
    TextView destReachedLabel;
    TextView directionsLabel;
    List<InterceptPoint> intersections;
    PointF startPoint;

    PositionListener mvPosListener = new PositionListener() {
        @Override
        public void originChanged(MapView source, PointF loc){ // When a new origin is set
            initialUserPoint.set(loc.x, loc.y); // Set the position of the origin
            startPoint = loc;
            AccelerometerEventListener.currentUserPoint.set(loc.x, loc.y);
            mv.setUserPoint(loc); // Set the initial point of the user to the origin
            pathPoints.add(loc); // Add the initial point to the path
            destReachedLabel.setText("YOU HAVE YET TO REACH YOUR DESTINATION!");
        }

        @Override
        public void destinationChanged(MapView source, PointF dest){
            destPoint.set(dest.x, dest.y); // Set the location of the destination point
            pathPoints.add(dest);
            NavigationalMap nm = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344.svg"); // Load the E2-3344 map into the Navigational Map
            intersections = nm.calculateIntersections(pathPoints.get(0), pathPoints.get(1)); // Find the intersections between the initial and final points
            if (intersections.isEmpty() == false){
                // If there are walls
                // Find the bottom of the wall
                float destX = dest.x;
                float destY = dest.y;

                float deltaX = destX - startPoint.x;
                if (deltaX > 0){
                    // Go Right
                    PointF fakeFinal = new PointF(); // Fake final
                    fakeFinal.x = destX;
                    fakeFinal.y = destY;

                    PointF movingPoint = new PointF(); // Moving point
                    movingPoint.x = startPoint.x;
                    movingPoint.y = startPoint.y;

                    List<InterceptPoint> obstacles;
                    do { // Scale the wall until it's clear
                        movingPoint.y += 0.5;
                        fakeFinal.y = movingPoint.y;
                        obstacles = nm.calculateIntersections(movingPoint, fakeFinal);
                    }while(obstacles.isEmpty() == false);
                    pathPoints.add(movingPoint); // Add point
                    pathPoints.add(fakeFinal); // Add point
                    obstacles = nm.calculateIntersections(fakeFinal, dest);
                    if (obstacles.isEmpty() == true){
                        pathPoints.add(dest); // Add the final point
                    }
                }else{
                    // Go Left
                    PointF fakeFinal = new PointF(); // Fake final
                    fakeFinal.x = destX;
                    fakeFinal.y = destY;

                    PointF movingPoint = new PointF(); // Moving point
                    movingPoint.x = startPoint.x;
                    movingPoint.y = startPoint.y;

                    List<InterceptPoint> obstacles;
                    do {
                        movingPoint.y += 0.5;
                        fakeFinal.y = movingPoint.y;
                        obstacles = nm.calculateIntersections(movingPoint, fakeFinal);
                    }while(obstacles.isEmpty() == false);
                    pathPoints.add(movingPoint); // Add point
                    pathPoints.add(fakeFinal); // Add point
                    obstacles = nm.calculateIntersections(fakeFinal, dest);
                    if (obstacles.isEmpty() == true){
                        pathPoints.add(dest); // Add the final point
                    }
                }

            }else{
                pathPoints.add(dest);
            }
           // pathPoints.add(dest);
            pathPoints.remove(1);
           mv.setUserPath(pathPoints);
            PathCreated = true;

            // Clear the pathPoints for the next time the destination is set
            for (PointF pf:pathPoints){
                directionPoints.add(pathPoints.indexOf(pf), pf);
            }
            pathPoints.clear();
            pathPoints.add(startPoint);
            destReachedLabel.setText("YOU HAVE YET TO REACH YOUR DESTINATION!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout); // Create the parent layout
        layout.setOrientation(LinearLayout.VERTICAL); // Set the orientation to vertical

        mv = new MapView(getApplicationContext(), 1200, 1200, 49, 50); // Create the map view
        registerForContextMenu(mv);

        // Add the Map View to the layout
        map = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344.svg"); // Load E2-3344 Map
        mv.setMap(map);
        layout.addView(mv);
        mv.setVisibility(View.VISIBLE);

        mv.addListener(mvPosListener); // Add the position listener

        // Instantiating accelerometer graph
        // Create the line graph that tracks the x, y, and z values of the accelerometer
        // accelGraph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        // layout.addView(accelGraph); // Add the graph to the parent layout
        // accelGraph.setVisibility(View.VISIBLE); // Make the graph visible

        // Create the line graph that tracks the x, y, and z values of the magnetic field
//        magFieldGraph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
//        layout.addView(magFieldGraph); // Add the graph to the parent layout
//        magFieldGraph.setVisibility(View.VISIBLE); // Make the graph visible

        // Accelerometer Label for outputting accelerometer readings

        destReachedLabel = new TextView(getApplicationContext());
        destReachedLabel.setTextColor(Color.BLACK);
        layout.addView(destReachedLabel);

        directionsLabel = new TextView(getApplicationContext());
        directionsLabel.setTextColor(Color.BLACK);
        layout.addView(directionsLabel);

        TextView accelSensorLabel = new TextView(getApplicationContext());
        accelSensorLabel.setTextColor(Color.BLACK);
        layout.addView(accelSensorLabel); // Add the label to the parent layout

        // Magnetic Field Label for outputting magnetic field sensor readings
        TextView magFieldSensorLabel = new TextView(getApplicationContext());
        magFieldSensorLabel.setTextColor(Color.BLACK);
        layout.addView(magFieldSensorLabel); // Add the label to the parent layout

        // Request the sensor manager
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get instances of each sensor with the sensor manager
        Sensor linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Get the accelerometer sensor
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magFSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); // Get the magnetic field sensor

        // Instantiate event listeners to receive the events from the sensors
        magFieldListener = new MagneticFieldSensorEventListener(magFieldSensorLabel);
        accelListener = new AccelerometerEventListener(accelSensorLabel, accelGraph, magFieldListener, destReachedLabel, directionsLabel);

        // Register each listener to the respective sensor
        sensorManager.registerListener(accelListener, linearAccelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magFieldListener, magFSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magFieldListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Button to reset the step account pedometer
        Button resetButton = new Button(getApplicationContext());
        resetButton.setText("Reset Step Count");
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accelListener.reset(); // Reset the accelerometer sensor
                magFieldListener.reset(); // Reset the displacement readings
            }
        });
        layout.addView(resetButton); // Add the button to the parent layout
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
    }

    // Method for data extraction and file write out
    /*private void writeToFile(float[] values){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            pw.append(values[0] + ", " + values[1] + ", " + values[2] + "\n");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
