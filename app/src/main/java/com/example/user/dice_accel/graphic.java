package com.example.user.dice_accel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class graphic extends AppCompatActivity {

    private LineGraphSeries<DataPoint> dice1, dice2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphic);
        setTitle("Graph Of The Dice");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GraphView graphicchart = (GraphView) findViewById(R.id.graph);
        dice1 = DiceData.dice1;
        dice1.setColor(Color.CYAN);
        dice2 = DiceData.dice2;
        dice2.setColor(Color.BLUE);
        graphicchart.addSeries(dice1);
        graphicchart.addSeries(dice2);
        graphicchart.refreshDrawableState();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}
