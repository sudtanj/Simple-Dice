/*  This file is part of Simple Dice.
 *
 *  Simple Dice is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Dice is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Dice.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.user.dice_accel;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

public class RollDice extends AppCompatActivity implements SensorEventListener {
	private final int rollAnimations = 50;
	private final int delayTime = 15;
	private Resources res;
	private final int[] diceImages = new int[] { R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6 };
	private Drawable dice[] = new Drawable[6];
	private final Random randomGen = new Random();
	@SuppressWarnings("unused")
	private int diceSum;
	private int roll[] = new int[] { 6, 6 };
	private ImageView die1;
	private ImageView die2;
	private LinearLayout diceContainer;
	private SensorManager sensorMgr;
	private Handler animationHandler;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	private boolean paused = false;
	private static final int UPDATE_DELAY = 50;
	private static final int SHAKE_THRESHOLD = 800;
    private graphic graph=new graphic();
	private RandomPredictor prediction;
    private TextToSpeech t1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		paused = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		setTitle("Simple Dice");
		res = getResources();
		for (int i = 0; i < 6; i++) {
			dice[i] = res.getDrawable(diceImages[i]);
		}
		diceContainer = (LinearLayout) findViewById(R.id.diceContainer);
		diceContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					rollDice();
				} catch (Exception e) {};
			}
		});
		die1 = (ImageView) findViewById(R.id.die1);
		die2 = (ImageView) findViewById(R.id.die2);
		animationHandler = new Handler() {
			public void handleMessage(Message msg) {
				die1.setImageDrawable(dice[roll[0]]);
				die2.setImageDrawable(dice[roll[1]]);
			}
		};
		prediction=new RandomPredictor();
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this,
				sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER),	SensorManager.SENSOR_DELAY_GAME);
		if (!accelSupported) sensorMgr.unregisterListener(this); //no accelerometer on the device
		rollDice();
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	public void makePrediction(){
        String output=new String((Math.abs(prediction.getPrediction())%6+1)+" "+(Math.abs(prediction.getPrediction())%6+1));
        t1.speak("The outcome prediction is "+output, TextToSpeech.QUEUE_FLUSH, null);
        Toast.makeText(getApplicationContext(),output.toString(),Toast.LENGTH_LONG).show();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), graph.getClass()));
                return true;
            case R.id.predict:
                makePrediction();
                return true;
			case R.id.aboutme:
				Toast.makeText(getApplicationContext(), "Created by dylanmtaylor \n Modified by \n - Bryan Christofel \n - Sudono Tanjung", Toast.LENGTH_SHORT).show();
				return true;
			default:
                return super.onOptionsItemSelected(item);
        }
    }
	private void rollDice() {
		if (paused) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < rollAnimations; i++) {
					doRoll();
				}
			}
		}).start();
		MediaPlayer mp = MediaPlayer.create(this, R.raw.roll);
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp.start();
        DiceData.addData(1,roll[0]+1);
        DiceData.addData(2,roll[1]+1);
	}

	private void doRoll() { // only does a single roll
		roll[0] = randomGen.nextInt(6);
		roll[1] = randomGen.nextInt(6);
		diceSum = roll[0] + roll[1] + 2; // 2 is added because the values of the rolls start with 0 not 1
		synchronized (getLayoutInflater()) {
			animationHandler.sendEmptyMessage(0);
		}
		try { // delay to alloy for smooth animation
			Thread.sleep(delayTime);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void onResume() {
		super.onResume();
		paused = false;
	}
	
	public void onPause() {
		super.onPause();
		paused = true;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor mySensor = event.sensor;
		if (mySensor.getType() == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			if ((curTime - lastUpdate) > UPDATE_DELAY) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
				z = event.values[SensorManager.DATA_Z];
				float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
				if (speed > SHAKE_THRESHOLD) { //the screen was shaked
					rollDice();
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		return; //this method isn't used
	}
}