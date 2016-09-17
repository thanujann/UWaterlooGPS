package lab1_202_03.uwaterloo.ca.lab4_202_03;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.provider.Settings;
import android.widget.TextView;

import ca.uwaterloo.sensortoy.LineGraphView;
import ca.uwaterloo.sensortoy.mapper.MapView;

// Accelerometer Sensor Event Listener
public class AccelerometerEventListener implements SensorEventListener{
    // Instantiate TextView and LineGraphView objects
    TextView output;
    TextView destReachedLabel;
    TextView directionsLabel;
    LineGraphView graph;
    MagneticFieldSensorEventListener magFieldListener;
    int pathVertex = 1; // Track the points already passed in the path

    // Constant for low pass filter
    final float CONSTANT = (float)0.75;

    // Instantiating the state machines states
    int currentState = 1;
    final int AWAITING_STATE = 1;
    final int STATE2 = 2;
    final int STATE3 = 3;
    final int STATE4 = 4;
    float[] currentValues = {0, 0, 0};
    int stepCount = 0;

    static PointF currentUserPoint = new PointF(MainActivity.initialUserPoint.x, MainActivity.initialUserPoint.y);
    PointF nextUserPoint = new PointF(0, 0);

    public AccelerometerEventListener(TextView outputView, LineGraphView graphView, MagneticFieldSensorEventListener magFieldListener, TextView destReachedLabel, TextView directionsLabel){
        output = outputView;
        graph = graphView;
        this.magFieldListener = magFieldListener;
        this.destReachedLabel = destReachedLabel;
        this.directionsLabel = directionsLabel;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){    }

    public void onSensorChanged(SensorEvent event){ // SensorEvent represents a single message from a single sensor
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Smooth values for x, y and z with a low pass filter
            currentValues[0] += (event.values[0] - currentValues[0]) / CONSTANT;
            currentValues[1] += (event.values[1] - currentValues[1]) / CONSTANT;
            currentValues[2] += (event.values[2] - currentValues[2]) / CONSTANT;

            // graph.addPoint(currentValues); // Add the current x, y, and z component readings to the graph
            pedometerFSM(); // Calling the state machine method

            // Output the step count
            output.setText(String.format("------ STEPS ------\n" +
                    "COUNT: " + stepCount));
        }
    }

    // Reset the step counter
    public void reset(){
        stepCount = 0;
    }

    // Returns current acceleration values
    public float[] getValues(){
        return currentValues;
    }

    // Tracks steps with a finite state machine
    private void pedometerFSM(){
        float value = currentValues[2];

        //Finite State Machine to track steps
        switch (currentState) {
            //Changes the state from nothing to peak if the stipulations are met
            case 1:
                if (value < -1.5){
                    currentState = STATE2;
                }
                break;
            //Changes the state from decreasing to increasing if the stipulations are met
            case 2:
                if (value > -1.5){
                    currentState = STATE3;
                }
                break;
            //Count a step if it adheres to the following conditions
            case 3:
                if (value > 2.4) {
                    currentState = STATE4;
                }
                break;
            //Checks if the value goes beyond a reasonable peak, if so, reset state, counts a step otherwise
            case 4:
                if(value > 7.5) {
                    currentState = AWAITING_STATE;
                } else if (value < 2.4) {
                    currentState = AWAITING_STATE;
                    stepCount++; // Increment the step counter
                    magFieldListener.updateNorthDisplacement(); // Update the displayed North displacement
                    magFieldListener.updateEastDisplacement(); // Update the displayed East displacement

                    nextUserPoint.set((float)(currentUserPoint.x + magFieldListener.getEastStepMag()*1.5),
                            (float) (currentUserPoint.y - magFieldListener.getNorthStepMag()*1.5)); // Set the user point, depending on the direction
                    if(updatePoint(currentUserPoint, nextUserPoint)) { // If there are no walls, move to the next point
                        currentUserPoint.x += (float) magFieldListener.getEastStepMag()*1.5;
                        currentUserPoint.y -= (float) magFieldListener.getNorthStepMag()*1.5;
                    } else { // If there is a wall, don't move and decrement the step that was added previously
                        stepCount--;
                    }
                    MainActivity.mv.setUserPoint(currentUserPoint); // Set the user point to the updated point
                    if (MainActivity.PathCreated){ // If a path has been created, output directions
                        float updatedPointX;
                        float updatedPointY;
                        updatedPointX = MainActivity.directionPoints.get(pathVertex).x - currentUserPoint.x; // Check how far from the destination the user is (X)
                        updatedPointY = currentUserPoint.y - MainActivity.directionPoints.get(pathVertex).y; // Check how far from the destination the user is (Y)
                        // Output the current distance from the destination, in steps
                        directionsLabel.setText(String.format("North: " + Math.ceil(Math.ceil(updatedPointY) / 2) + " steps\nEast: " + Math.ceil(Math.ceil(updatedPointX) / 2) + " steps"));
                        if (Math.abs(updatedPointX) < 1 && Math.abs(updatedPointY) < 1){  // Check if the value is close enough
                            pathVertex++; // Move to next point in the path
                            if (pathVertex >= MainActivity.directionPoints.size()){ // Check if the path is done
                                MainActivity.PathCreated = false; // Stop incrementing the steps
                            }else {
                                // Once the destination is reached, update the user with the steps required to reach the next destination
                                updatedPointX = MainActivity.directionPoints.get(pathVertex).x - currentUserPoint.x;
                                updatedPointY = currentUserPoint.y - MainActivity.directionPoints.get(pathVertex).y;
                                directionsLabel.setText(String.format("North: " + Math.ceil(Math.ceil(updatedPointY) / 2) + " steps\nEast: " + Math.ceil(Math.ceil(updatedPointX) / 2) + " steps"));
                            }
                        }
                    }
                    if(checkDestReached(currentUserPoint)){ // If the destination is reached, output to the user
                        destReachedLabel.setText("DESTINATION REACHED!");
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean updatePoint(PointF initialPoint, PointF finalPoint) { // Return true if there are no walls between the current and next points
        if(MainActivity.map.calculateIntersections(initialPoint, finalPoint).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkDestReached (PointF currentUserPoint){ // Returns true if the destination had been reached
        if(currentUserPoint.x >= (MainActivity.destPoint.x - 1.5) && currentUserPoint.x <= (MainActivity.destPoint.x + 1.5)){ // Check the X range
            if(currentUserPoint.y >= (MainActivity.destPoint.y - 1.5) && currentUserPoint.y <= (MainActivity.destPoint.y + 1.5)){ // Check the Y range
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}