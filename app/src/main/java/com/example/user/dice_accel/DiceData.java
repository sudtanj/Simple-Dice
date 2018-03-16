package com.example.user.dice_accel;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Created by User on 11/11/2017.
 */

public class DiceData {
    public static LineGraphSeries<DataPoint> dice1=new LineGraphSeries<>(new DataPoint[] {
    }),dice2=new LineGraphSeries<>(new DataPoint[] {
    });
    public static int counter1=0,counter2=0;

    public static void addData (int point, int data){
        if(point==1){
            dice1.appendData(new DataPoint(counter1, data),true,10);
            counter1++;
        }else{
            dice2.appendData(new DataPoint(counter2, data),true,10);
            counter2++;
        }
    }
}
