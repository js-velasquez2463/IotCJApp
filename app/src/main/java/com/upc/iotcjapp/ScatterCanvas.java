package com.upc.iotcjapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ScatterCanvas extends ScatterChart {

    public List<Entry> entries;

    private MapCanvas instance;
    private HashMap<String, Entry> entryMap;

    public boolean showVelocity;

    public ScatterCanvas(Context context) {
        super(context);
        initChart();
    }

    public ScatterCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public ScatterCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    private void initChart() {
        // Create an empty list of entries
        entries = new ArrayList<>();
        entryMap = new HashMap<>();

        // Add some sample data points
        entries.add(new Entry(0f, 0f));

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

        ScatterDataSet dataSet = new ScatterDataSet(entries, "Trayectoria");
        dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE); // Forma de los puntos
        dataSet.setColor(Color.RED); // Color de los puntos
        dataSet.setScatterShapeSize(8f); // Tamaño de los puntos

        ScatterData scatterData = new ScatterData(dataSet);
        setData(scatterData);

        Log.d("Chart Initialization", "Success");
    }

    public void drawPoint(float x, float y) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);

        String formattedX = decimalFormat.format(x);
        String formattedY = decimalFormat.format(y);

        x = Float.parseFloat(formattedX);
        y = Float.parseFloat(formattedY);

        String key = generateKey(x, y);
        if (entryMap.containsKey(key)) {
            Log.d("Scatter", "Ya existia x: "+ x + " :: y: " + y);
            // El punto ya existe en el mapa, no es necesario agregarlo nuevamente
            return;
        }

        ScatterData lineData = getData();
        if (lineData != null) {
            ScatterDataSet dataSet = (ScatterDataSet) lineData.getDataSetByIndex(0);

            if (dataSet != null) {
                try {
                    Entry entry = new Entry(x, y);
                    entryMap.put(key, entry);
                    entries.add(new Entry(x, y));
                    Log.d("Scatter", "Trying to insert x: "+ x + " :: y: " + y);
                    Collections.sort(entries, new EntryXComparator()); // Sort entries by x value
                    dataSet.setValues(entries);
                    lineData.notifyDataChanged();
                    notifyDataSetChanged();
                    invalidate();
                } catch (Exception e) {

                }

            }
        }
    }

    public void restartPoints() {
        ScatterData lineData = getData();
        if (lineData != null) {
            entries.clear();
            entryMap.clear();
            ScatterDataSet dataSet = (ScatterDataSet) lineData.getDataSetByIndex(0);
            dataSet.clear();
            invalidate();
        }
    }

    private String generateKey(float x, float y) {
        return String.format("%.2f,%.2f", x, y);
    }

    public class EntryXComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry entry1, Entry entry2) {
            float x1 = entry1.getX();
            float x2 = entry2.getX();
            return Float.compare(x1, x2);
        }
    }
}