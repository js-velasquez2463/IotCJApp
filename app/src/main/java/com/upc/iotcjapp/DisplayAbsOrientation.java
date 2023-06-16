package com.upc.iotcjapp;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.Arrays;

public class DisplayAbsOrientation extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gravitymeter;

    private int sig_mag=1;

    private float[] mag=new float[3];   // readings magnetometer
    private float[] acc=new float[3];   // readings accelerometer
    private float[] acc_1={0f,0f,0f};       // previous, for Low Pass filter
    private float[] mag_1={0f,0f,0f};       // previous, for Low Pass filter


    private float[] B = {0f,0f,0f};     // Normalized to length 1, MAGNETIC READING in DEV coordinates
    private  float[] E = {0f,0f,0f};    // EAST (Y) in DEV coordinates
    private float[] N = {0f,0f,0f};     // NORTH (Y) in DEV coordinates
    private float[] Zaxis = {0f,0f,0f};     // Vertical axis in DEV coordinates

    // external points
    private float time=0.0f;                // time for movinig points
    private float[] point_earth = {0f,10f,0.5f};  // point 1 in EARTH coordinates
    private float[] point_dev = {0f,0f,0f};         // POINT in DEV coordinates
    private float[] point2_earth = {0f,10f,0.5f};  // point 2 in EARTH coordinates
    private float[] point2_dev = {0f,0f,0f};         // POINT in DEV coordinates

    // For filtering purposes
    private String message;
    private float alpha=0.975f;             // coefficient of low-pass filter
    private boolean filterActivate;
    private String check = "Inicial";

    private float[] ditances = {0f,0f};

    private DrawView drawView;  // screen CANVAS

    private MapCanvas mapCanvas;


    private float[] gravity = new float[3];  // Valores de la gravedad filtrada
    private float[] linear_acceleration = new float[3];  // Valores de la gravedad filtrada
    private float[] geomagnetic = new float[3];  // Valores del campo magnético filtrado
    private float[] rotationMatrix = new float[9];  // Matriz de rotación
    private float[] orientation = new float[3];  // Orientación del dispositivo
    private float[] accelerationXY = new float[2];


    private float[] accelerometerData = new float[3];
    private float[] magneticFieldData = new float[3];
    private float[] accelerometerData_1 = {0f,0f,0f};
    private float[] magneticFieldData_1  ={0f,0f,0f};

    private float[] magneticField = new float[3];
    private float[] previousAcceleration = new float[3];
    private float[] distanceXY = new float[2];
    private float alpha2 = 0.8f;
    float[] worldAcceleration = new float[3];

    float minValue = Float.MAX_VALUE;
    float maxValue = Float.MIN_VALUE;

    float minValueR = Float.MAX_VALUE;
    float maxValueR = Float.MIN_VALUE;

    private float[] gravityValues = null;
    private float[] magneticValues = null;

    private long lastTimestamp = 0;
    private float distanceX = 0.0f;
    private float distanceY = 0.0f;

    int cantidadCiclos = 0;
    float promAccX = 0.0f;
    float promAccY = 0.0f;

    private void calculateWell (SensorEvent event) {
        if ((gravityValues != null) && (magneticValues != null)
                && (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {

            float[] deviceRelativeAcceleration = new float[4];
            deviceRelativeAcceleration[0] = event.values[0];
            deviceRelativeAcceleration[1] = event.values[1];
            deviceRelativeAcceleration[2] = event.values[2];
            deviceRelativeAcceleration[3] = 0;

            // Change the device relative acceleration values to earth relative values
            // X axis -> East
            // Y axis -> North Pole
            // Z axis -> Sky

            float[] R = new float[16], I = new float[16], earthAcc = new float[16];

            SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);

            float[] inv = new float[16];

            android.opengl.Matrix.invertM(inv, 0, R, 0);
            android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
            //Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
            long currentTimestamp = System.currentTimeMillis();
            long diferencia = currentTimestamp - lastTimestamp;


            //Log.d("acaaaaa", lastTimestamp + " ::: " + diferencia);

            if (diferencia >= 1000.0f) {
                float deltaTime = (currentTimestamp - lastTimestamp) / 1000.0f;
                float promX = promAccX / cantidadCiclos;
                float promY = promAccY / cantidadCiclos;

                // Calcular las distancias en los ejes X e Y
                float deltaX = 0.5f * promX * deltaTime * deltaTime;
                float deltaY = 0.5f * promY * deltaTime * deltaTime;



                // Acumular las distancias recorridas
                distanceX += deltaX;
                distanceY += deltaY;

                Log.d("distancias", distanceX + " ::: " + distanceY);
                Log.d("tiempo", deltaTime + " ::: " + promX + " -- " + promY);

                updateView(distanceX, distanceY);

                cantidadCiclos = 0;
                promAccX =0;
                promAccY = 0;
                lastTimestamp = currentTimestamp;
            } else {
                cantidadCiclos++;
                promAccX += earthAcc[0];
                promAccY += earthAcc[1];
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            //Log.d("grabvittyy", Arrays.toString(event.values));
            gravityValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values;
            //Log.d("magnetiicc", Arrays.toString(event.values));
        }
    }


    private void calculateOrientation() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magneticFieldData);
        SensorManager.getOrientation(rotationMatrix, orientation);
    }

    private void updateView(float x, float y) {
        // Actualizar la vista con las coordenadas x e y
        // ...
        //drawView.setDistanceXY(x, y);
        //mapCanvas.drawPoint(x, y);
    }

    private void calculateDistance() {
        float x = accelerometerData[0];
        float y = accelerometerData[1];

        // Eliminar la componente de la gravedad en los ejes X e Y
        float gravityX = (float) (gravity[0] * Math.cos(orientation[2]) +
                gravity[1] * Math.sin(orientation[2]));
        float gravityY = (float) (gravity[1] * Math.cos(orientation[2]) -
                gravity[0] * Math.sin(orientation[2]));

        // Calcular los movimientos en los ejes X e Y
        float deltaX = x - gravityX;
        float deltaY = y - gravityY;

        // Actualizar los valores de movimiento en los ejes X e Y
        distanceXY[0] += deltaX;
        distanceXY[1] += deltaY;


        // Actualizar la vista con las coordenadas x e y acumuladas
        try {

            //Matrix.multiplyMV(worldAcceleration, 0, rotationMatrix, 0, accelerometerData, 0);

            worldAcceleration = multiplyMatrixVector(convertVectorToMatrix(rotationMatrix, 3), accelerometerData);


            updateView(worldAcceleration[0], worldAcceleration[1]);
            float threshold = 0f;

            float total = gravity[0] + gravity[1] + gravity[2];
            float totalReal= Math.abs(worldAcceleration[0]) + Math.abs(worldAcceleration[1]);



            //Log.d("ACELERO HATSA", Arrays.toString(worldAcceleration) + " max: " + Arrays.toString(gravity) + " min: " + Arrays.toString(magneticFieldData));

            if (total > maxValue) {
                maxValue = total;
                Log.d("ACELERO HATSA 22", total + " max: " + maxValue + " min: " + minValue + " -- real: "+ totalReal);
            }
            if (total < minValue) {
                minValue = total;
                Log.d("ACELERO HATSA 222", total + " max: " + maxValue + " min: " + minValue + " -- real: "+ totalReal);
            }

            if (totalReal > maxValueR) {
                maxValueR = totalReal;
                Log.d("REAL ACELERO HATSA 221", total+ " max: " + maxValueR + " min: " + minValueR + " -- real: "+ totalReal);
            }
            if (totalReal < minValueR) {
                minValueR = totalReal;
                Log.d("REAL ACELERO HATSA 222", total + " max: " + maxValueR + " min: " + minValueR + " -- real: "+ totalReal);
            }
            //Log.d("MATT", Arrays.toString(worldAcceleration));

        } catch (Exception e) {
            updateView(-99, -99);
            Log.d("ERROR", e.toString());
        }
    }



    public void setDistancBetter (SensorEvent event, DrawView drawView) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            gravity[0] = alpha2 * gravity[0] + (1 - alpha2) * event.values[0];
            gravity[1] = alpha2 * gravity[1] + (1 - alpha2) * event.values[1];
            gravity[2] = alpha2 * gravity[2] + (1 - alpha2) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            copyarrayto(event.values,accelerometerData);

            lowpassfilter(accelerometerData, accelerometerData_1, alpha);
            copyarrayto(accelerometerData, accelerometerData_1);

            calculateOrientation();
            calculateDistance();
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            copyarrayto(event.values, magneticFieldData);
            if(filterActivate) {
                lowpassfilter(magneticFieldData, magneticFieldData_1, alpha);
                copyarrayto(magneticFieldData, magneticFieldData_1);
            }

            calculateOrientation();
            calculateDistance();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravitymeter=sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if (message.equals("nofilter")){
            filterActivate=false;
        }
        if (message.equals("filter")){
            filterActivate=true;
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Set size of screen etc for Drawing
        int RADIUS= (int) width/3;
        drawView = new DrawView(this);
        drawView.setRadius(RADIUS);
        drawView.setCenterXY(width/2, height/2);
        drawView.setVerticalAxisOval(RADIUS);
        setContentView(drawView);

        Log.d("passo", "primerrooo");
        //mapCanvas = new MapCanvas(this);

        Log.d("passo", "segundooo");

        //setContentView(mapCanvas);


        // Capture the layout's TextView and set the string as its text
        //TextView textView = findViewById(R.id.textView);
        //textView.setText(message);
    }


    //@Override
    /*public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    */


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (lastTimestamp == 0) {
            lastTimestamp = System.currentTimeMillis();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            copyarrayto(event.values,acc);
            if(filterActivate) {
                lowpassfilter(acc, acc_1, alpha);
                copyarrayto(acc, acc_1);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            copyarrayto(event.values,mag);
            if(filterActivate) {
                lowpassfilter(mag, mag_1, alpha);
                copyarrayto(mag, mag_1);
            }
        }

        // find axis in DEV coordinates
        copyarrayto(acc, Zaxis);
        //normalize(acc);
        normalize(Zaxis);

        copyarrayto(mag,B);
        normalize(B);

        crossProduct(B,Zaxis,E);
        normalize(E);

        crossProduct(Zaxis,E,N);
        normalize(N);


        time= time+1;                           // point 1 is moving in a circle

        point_earth[0]= 10*(float)Math.cos((float)2*3.1415*time/3000);
        point_earth[1]= 10*(float)Math.sin((float)2*3.1415*time/3000);

        // multiply times rotation matrix [Edev Ndev Zdev]
        multRotationMat(point_earth,E,N,Zaxis,point_dev);
        multRotationMat(point2_earth,E,N,Zaxis,point2_dev);


        // drawing
        drawView.setVectorsXYZ(E[0], E[1], E[2], N[0], N[1], N[2], Zaxis[0], Zaxis[1], Zaxis[2]);
        drawView.setPoint(point_dev[0], point_dev[1], point_dev[2]);

        this.calculateWell(event);

        //setContentView(drawView);

    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    @Override
    protected void onResume(){
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors

        sensorManager.registerListener((SensorEventListener) this,accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener((SensorEventListener) this,magnetometer,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener((SensorEventListener) this,gravitymeter,
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this,accelerometer);
        sensorManager.unregisterListener((SensorEventListener) this,magnetometer);
        sensorManager.unregisterListener((SensorEventListener) this,gravitymeter);

    }



    private void crossProduct(float x[], float y[], float z[]){
        z[0] = x[1]*y[2] - x[2]*y[1];   // cross product
        z[1] = x[2]*y[0] - x[0]*y[2];
        z[2] = x[0]*y[1] - x[1]*y[0];

    }

    private float normalize(float x[]){
        float mod = (float) Math.sqrt(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]);
        if(mod!=0.0f) {
            int i;
            for (i = 0; i <= 2; i++) {
                x[i] = x[i] / mod;            // normalize
            }
        }
        return mod;
    }

    private void copyarrayto(float x[], float y[]) {
        int i;
        for (i = 0; i <= 2; i++) {
            y[i] = (float) x[i];
        }
    }

    private void arraytovectorstring(float x[], String y[]) {
        int i;
        for (i=0;i<=2; i++) {
            y[i] = String.format("%.2f", x[i]);
        }
    }
    private void lowpassfilter(float x[], float x_1[], float alpha){
        int i;
        for (i=0; i<=2; i++) {
            x[i]=(1.0f-alpha)*x[i]+alpha*x_1[i];
        }
    }

    private void multRotationMat(float x_earth[], float RX[], float RY[], float RZ[], float x_dev[]){
        int i;
        for (i=0;i<=2; i++) {
            x_dev[i] = x_earth[0]*RX[i]+x_earth[1]*RY[i]+x_earth[2]*RZ[i] ;
        }

    }

    public static float[] multiplyMatrixVector(float[][] matrix, float[] vector) {
        int mRows = matrix.length;
        int mCols = matrix[0].length;

        if (mCols != vector.length) {
            throw new IllegalArgumentException("Matrix and vector dimensions do not match");
        }

        float[] result = new float[mRows];

        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mCols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }

        return result;
    }

    public static float[][] convertVectorToMatrix(float[] vector, int numRows) {
        int vectorLength = vector.length;
        int numCols = (int) Math.ceil((float) vectorLength / numRows);

        float[][] matrix = new float[numRows][numCols];

        int vectorIndex = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (vectorIndex < vectorLength) {
                    matrix[i][j] = vector[vectorIndex];
                    vectorIndex++;
                } else {
                    // Fill any remaining elements with zeros
                    matrix[i][j] = 0.0f;
                }
            }
        }

        return matrix;
    }

}

