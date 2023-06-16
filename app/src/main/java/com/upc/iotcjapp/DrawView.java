package com.upc.iotcjapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public  class DrawView extends View {

    private int radius; //500
    private float centerX; //1000 550
    private float centerY; //5000 1000
    private int verticalAxisOval; // = radius

    // we draw three vectors and one point
    private float vectorPaintX[]={1f,0f,0f};
    private float vectorPaintY[]= {0f,1f,0f};
    private float vectorPaintZ[]= {0f,0f,1f};
    private float vectorPoint[]={0f,0.5f,-0.5f};

    public float[] previousAcc = {0f,0f,0f};

    public float[] accelerationXY = new float[2];

    public String accelerometerText = "";


    private Color colPaint;

    private Paint paint = new Paint();

    Paint textPaint = new Paint();

    public DrawView(Context context) {
        super(context);
        paint.setColor(Color.WHITE);
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(30);
    }

    public void setRadius(int r){
        radius=r;
    }


    public void setVerticalAxisOval(int r){
        verticalAxisOval=r;
    }

    public void setCenterXY(float cx, float cy){
        centerX=cx;
        centerY=cy;
    }

    public void setVectorsXYZ(float x0, float x1, float x2, float y0, float y1, float y2, float z0, float z1, float z2){
        // x and y are in SENSOR device coordenates, vectorPAints i SCREEN coodinates
        vectorPaintX[0] = x0;vectorPaintX[1] = x1;vectorPaintX[2] = x2;  // EAST to x device
        vectorPaintY[0] = y0;vectorPaintY[1] = y1;vectorPaintY[2] = y2; // NORTH to -z device
        vectorPaintZ[0] = z0;vectorPaintZ[1] = z1;vectorPaintZ[2] = z2;

    }

    public void setDistanceXY(float x, float y){
        accelerationXY = new float[]{x, y};
    }

    public void setPoint(float x0, float x1, float x2){
        vectorPoint[0]=x0;
        vectorPoint[1]=x1;
        vectorPoint[2]=x2;
    }

    public void setColor(Color col){
        colPaint=col;
    }

    public String normalizarValores(float[] acc) {
        // Dentro del método onSensorChanged()
        float[] diff = new float[3];
        for (int i = 0; i < 3; i++) {
            diff[i] = Math.abs(acc[i] - previousAcc[i]);
        }

        System.arraycopy(acc, 0, previousAcc, 0, 3);

        // Calcula la cantidad de decimales y el umbral
        int decimalDigits = 2; // Número de decimales a mostrar
        float threshold = 0.1f; // Umbral para considerar el cambio como significativo

        float diffSum = diff[0] + diff[1] + diff[2];
        if (diffSum > threshold) {
           return "Acelerómetro: x = " + String.format("%." + decimalDigits + "f", acc[0]) + ", y = " + String.format("%." + decimalDigits + "f", acc[1]) + ", z = " + String.format("%." + decimalDigits + "f", acc[2]);
        }
        return "Acelerómetro: x = 0, y = 0, z = 0";
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        float textX = centerX - radius;
        float textY = centerY + radius + 40;
                                                        // Draw external frame
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        canvas.drawCircle(centerX,centerY,radius,paint);
        canvas.drawLine(centerX-radius, centerY, centerX+radius, centerY, paint);
        canvas.drawLine(centerX, centerY-radius, centerX, centerY+radius, paint);


        paint.setStrokeWidth(20);       // Draw normalized EAST vector
        paint.setColor(Color.GREEN);
        canvas.drawLine(centerX, centerY, vectorPaintX[0]*radius+centerX, (-vectorPaintX[1])*radius+centerY, paint);

        paint.setStrokeWidth(20);       // Draw normalized NORTH vector
        paint.setColor(Color.RED);
        canvas.drawLine(centerX, centerY, vectorPaintY[0]*radius+centerX, (-vectorPaintY[1])*radius+centerY, paint);

        paint.setStrokeWidth(20);       //  Draw normalized Zearth vector
        paint.setColor(Color.BLUE);
        canvas.drawLine(centerX, centerY, vectorPaintZ[0]*radius+centerX, (-vectorPaintZ[1])*radius+centerY, paint);


        if (vectorPoint[2]<0) {                 // point 1 (Moving in circles)
            paint.setColor(Color.BLACK);        // pasa frente a nosotros
        } else{
            paint.setColor(Color.RED);          // pasa por detrás
        }
        paint.setStrokeWidth(40);
        canvas.drawLine(vectorPoint[0] * radius +centerX , (-vectorPoint[1] ) * radius + centerY -20,  vectorPoint[0] * radius + centerX, (-vectorPoint[1]) * radius + centerY+20, paint);

        accelerometerText = normalizarValores(new float[]{vectorPaintX[0], vectorPaintY[0], vectorPaintZ[0]});

        int decimalDigits = 2; // Número de decimales a mostrar
        float threshold = 0.1f; // Umbral para considerar el cambio como significativo
        String accCords = "Acelerómetro: x = " + String.format("%." + decimalDigits + "f", accelerationXY[0]) + ", y = " + String.format("%." + decimalDigits + "f", accelerationXY[1]);
        canvas.drawText(accCords, textX, textY, textPaint);

    }


}

