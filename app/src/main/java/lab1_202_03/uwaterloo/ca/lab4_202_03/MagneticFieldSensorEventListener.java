package lab1_202_03.uwaterloo.ca.lab4_202_03;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import ca.uwaterloo.sensortoy.LineGraphView;

// Magnetic Field Sensor Event Listener
public class MagneticFieldSensorEventListener implements SensorEventListener {
    TextView output;

    float[] magFieldValues = new float[3];
    float[] accelValues = new float[3];
    float[] R = new float[9]; // Rotational Matrix R
    float[] I = new float[9]; // Rotational Matrix I
    float[] orientation = new float[3];

//    final float CONSTANT = 0.5f;

    double northDisplacement = 0;
    double eastDisplacement = 0;
    double northStepMag = 0;
    double eastStepMag = 0;

    double mAzimuth = 0;
    double prevAngle =0;
    boolean initial = true;

    public MagneticFieldSensorEventListener(TextView outputView) {
        output = outputView;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){    }

    public void onSensorChanged(SensorEvent event){ // SensorEvent represents a single message from a single sensor
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Smooth values for x, y and z
//            accelValues[0] += (event.values[0] - accelValues[0]) / CONSTANT;
//            accelValues[1] += (event.values[1] - accelValues[1]) / CONSTANT;
//            accelValues[2] += (event.values[2] - accelValues[2]) / CONSTANT;
            accelValues = event.values.clone(); // Copy the accelerometer readings
        }

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magFieldValues = event.values.clone();

            //Smooth values for x, y and z
//            magFieldValues[0] += (event.values[0] - magFieldValues[0]) / CONSTANT;
//            magFieldValues[1] += (event.values[1] - magFieldValues[1]) / CONSTANT;
//            magFieldValues[2] += (event.values[2] - magFieldValues[2]) / CONSTANT;

            if(SensorManager.getRotationMatrix(R, I, accelValues, magFieldValues)){ // Get the rotation matrix
//                mAzimuth= (int) ( Math.toDegrees( SensorManager.getOrientation( R, orientation )[0] ) + 360 ) % 360;
                mAzimuth = SensorManager.getOrientation( R, orientation )[0]; // Get the orientation of the device

                if (initial){ // If initial reading of angle, then store the value in the previous angle variable as well
                    prevAngle = mAzimuth;
                    initial = false; // Set initial flag to false
                }
//                if ((mAzimuth - prevAngle) <= -Math.PI*1.9){
//                    prevAngle += 2*Math.PI;
//                } else if((mAzimuth - prevAngle) >= Math.PI*1.9){
//                    prevAngle -= 2*Math.PI;
//                }
                prevAngle += (mAzimuth - prevAngle)/2; // Average the previous angle with the difference between the current and previous angle
            }

            northStepMag = Math.cos(prevAngle); // Get the y component of the orientation vector (North)
            eastStepMag = Math.sin(prevAngle); // Get the x component of the orientation vector (East)

            //orientation[0] = (float) Math.toDegrees((prevAngle + 360))%360; // Converting angle to degrees between 0 and 360
            //graph.addPoint(orientation); // Add the angle to the graph

            // Output x, y, and z components of the magnetic field sensor, as well as the north and east displacement
            output.setText(String.format("------ DISPLACEMENT ------\nNORTH: %f\nEAST: %f",
                    northDisplacement, eastDisplacement));

        }
    }

    public void reset(){ // Method for resetting the displacement values
        northDisplacement = 0;
        eastDisplacement = 0;
        initial = false;
    }

    public void updateNorthDisplacement(){ // Update the North displacement displayed on the device
        northDisplacement += northStepMag;
    }

    public void updateEastDisplacement(){ // Update the East displacement displayed on the device
        eastDisplacement += eastStepMag;
    }

    public double getNorthDisplacement() {
        return northDisplacement;
    }

    public double getEastDisplacement() {
        return eastDisplacement;
    }

    public double getNorthStepMag() {
        return northStepMag;
    }

    public double getEastStepMag() {
        return eastStepMag;
    }
}