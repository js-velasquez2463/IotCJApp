package com.upc.iotcjapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MapCanvas extends LineChart {

    public float totalTiempo;
    public List<Entry> entries;
    public List<Entry> entries2;

    private MapCanvas instance;

    public MapCanvas(Context context) {
        super(context);
        initChart();
    }

    public MapCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public MapCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    private void initChart() {
        // Create an empty list of entries
        entries = new ArrayList<>();
        entries2 = new ArrayList<>();

        // Add some sample data points
        entries.add(new Entry(0f, 0f));
        entries2.add(new Entry(0f, 0f));


        // Set the data and styling for the line chart
        LineDataSet dataSet = new LineDataSet(entries, "Path");


        dataSet.setDrawIcons(false);
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);


        XAxis xAxis = getXAxis();
        xAxis.setGridColor(Color.WHITE);
        xAxis.setEnabled(true); // Habilitar el eje X
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Posicionar el eje X en la parte inferior
        xAxis.setDrawAxisLine(true); // Dibujar la línea del eje X
        xAxis.setDrawGridLines(false); // No dibujar las líneas de la cuadrícula del eje X
        xAxis.setGranularity(1f); // Establecer la separación entre etiquetas en el eje X
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(Color.WHITE);

        // Configurar el eje Y
        YAxis yAxis = getAxisLeft();
        yAxis.setEnabled(true); // Habilitar el eje Y
        yAxis.setDrawAxisLine(true); // Dibujar la línea del eje Y
        yAxis.setDrawGridLines(true); // Dibujar las líneas de la cuadrícula del eje Y
        yAxis.setGranularity(1f);
        yAxis.setDrawLabels(true);
        yAxis.setTextColor(Color.WHITE);

        entries.add(new Entry(0f, 0f));

        dataSet = new LineDataSet(entries, "Acceleration x");

        LineData lineData = new LineData(dataSet);

        //setData(lineData);

        Legend legend = getLegend();
        legend.setTextColor(Color.WHITE);

        // Crear el nuevo LineDataSet
        LineDataSet dataSet2 = new LineDataSet(entries2, "Acceleration y");
        dataSet2.setColor(Color.GREEN);
        dataSet2.setLineWidth(2f);
        dataSet2.setDrawCircles(false);
        dataSet2.setDrawCircleHole(false);
        dataSet2.setDrawValues(false);

        lineData.addDataSet(dataSet2);

        setData(lineData);

        Log.d("Chart Initialization", "Success");
    }

    public void drawPoint(float deltaT, float x, float y) {
        LineData lineData = getData();
        if (lineData != null) {
            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);
            LineDataSet dataSet2 = (LineDataSet) lineData.getDataSetByIndex(1);

            if (dataSet != null) {
                totalTiempo += deltaT;
                dataSet.addEntry(new Entry(totalTiempo, x));
                dataSet2.addEntry(new Entry(totalTiempo, y));
                lineData.notifyDataChanged();
                notifyDataSetChanged();
                invalidate();
            }
        }
    }

    public void restartPoints() {
        LineData lineData = getData();
        if (lineData != null) {
            entries.clear();
            entries2.clear();
            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);
            LineDataSet dataSet2 = (LineDataSet) lineData.getDataSetByIndex(1);
            dataSet.clear();
            dataSet2.clear();
            invalidate();
        }
    }
}