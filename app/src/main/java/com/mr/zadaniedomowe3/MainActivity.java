package com.mr.zadaniedomowe3;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.abs;
import static java.lang.Math.min;

class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static Sensor mSensor;
    private boolean start = false;
    private int sensorType;
    private long lastUpdate = -1;
    private ImageView ballEmpty;
    private ImageView ballFront;
    private TextView ballAnswer;

    private int screenWidth;
    private int screenHeight;
    private int imgEdgeSize;
    private boolean layoutReady;
    private ConstraintLayout mainContainer;
    private Path upPath;
    private Path downPath;
    private boolean animFlag = false;
    private static SensorManager mSensorManager;
    private int rand;
    private String[] answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        answers = getResources().getStringArray(R.array.answers);
        ballEmpty = findViewById(R.id.ball_empty);
        ballFront = findViewById(R.id.ball_front);
        ballAnswer = findViewById(R.id.answer_txt);
        ballEmpty.setVisibility(View.INVISIBLE);
        ballAnswer.setVisibility(View.INVISIBLE);
        ballFront.setVisibility(View.VISIBLE);
      //  sensorType = Sensor.TYPE_ACCELEROMETER;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensor == null) {
            Toast.makeText(this, "No accelerometer", Toast.LENGTH_SHORT).show();}
        }



        layoutReady = false;
        mainContainer = findViewById(R.id.sensor);
        mainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgEdgeSize = ballEmpty.getWidth();
                screenWidth = mainContainer.getWidth();
                screenHeight = mainContainer.getHeight();
                mainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layoutReady = true;
            }
        });
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeMicro;
        if(lastUpdate == -1){
            lastUpdate = event.timestamp;
            timeMicro = 0;
        } else {
            timeMicro = (event.timestamp - lastUpdate)/1000L;
            lastUpdate = event.timestamp;
        }


        if(layoutReady) {

                    handleAccelerationSensor(event.values[0]);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();

        if(mSensor != null)
            mSensorManager.registerListener(this, mSensor, 100000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mSensor != null)
            mSensorManager.unregisterListener(this, mSensor);

    }

    private void handleAccelerationSensor (final float sensorValue) {
        if (!animFlag) {
            if (abs(sensorValue) > 5) {
                start = true;
                animFlag = true;

                ballEmpty.setVisibility(View.INVISIBLE);
                ballFront.setVisibility(View.VISIBLE);
                ballAnswer.setVisibility(View.INVISIBLE);

                FlingAnimation flingX = new FlingAnimation(ballFront, DynamicAnimation.X);

                flingX.setStartVelocity(-1 * sensorValue * screenWidth / 2f)
                        .setMinValue(5)
                        .setMaxValue(screenWidth - imgEdgeSize +1)
                        .setFriction(1f);



                

                flingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                        if (v1 != 0) {
                            FlingAnimation reflingX = new FlingAnimation(ballFront, DynamicAnimation.X);

                            reflingX.setStartVelocity(-1 * v1)
                                    .setMinValue(5)
                                    .setMaxValue(screenWidth - imgEdgeSize +1)
                                    .setFriction(1.25f)
                                    .start();

                            reflingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                                @Override
                                public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                                    animFlag = false;
                                    rand = ((int) abs((sensorValue *100))) % 20;
                                }
                            });
                        } else {
                            animFlag = false;
                            start = false;
                        }
                    }
                });
                flingX.start();
            } else if (start) {
                ballEmpty.setVisibility(View.VISIBLE);
                ballFront.setVisibility(View.INVISIBLE);
                ballAnswer.setText(answers[rand]);
                ballAnswer.setVisibility(View.VISIBLE);
                start = false;
            }
        }
    }
}
