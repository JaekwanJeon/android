package com.example.tb.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.gesture.ChartScroller;
import lecho.lib.hellocharts.gesture.ChartTouchHandler;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.bt_generate)
    Button bt_generate;

    @BindView(R.id.bt_show)
    Button bt_show;

    HandlerThread thread;
    Handler generator;

    HandlerThread thread2;
    Handler sender;

    public static final String SEND_GRAPH_DATA_BROADCAST = "SEND_GRAPH_DATA_BROADCAST";
    public static final String EXTRA_VALUE = "EXTRA_VALUE";

    private static ArrayList<Integer> value = new ArrayList<>();

    private final int MIN_VALUE = 500;
    private final int MAX_VALUE = 2000;

    int mode = 0; // 1 : down, 0 : up
    int current_value = MIN_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        thread = new HandlerThread("Generator");
        thread.start();

        generator = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Random random = new Random();
                synchronized (value) {
                    if(mode == 0) { // up
                        if(current_value > MAX_VALUE) mode = 1; // down
                        int count = random.nextInt(6);
                        for(int i=0;i<count;i++) {
                            current_value += random.nextInt(80);
                            value.add(current_value);
                        }
                    } else { // down
                        if(current_value < MIN_VALUE) mode = 0; // up
                        int count = random.nextInt(6);
                        for(int i=0;i<count;i++) {
                            current_value -= random.nextInt(80);
                            value.add(current_value);
                        }
                    }

                    for(int i=0;i<value.size();i++) {
                        Log.i(TAG, "[GENE] : (" + i + ") [" + value.get(i) + "]");
                    }
                    generator.sendEmptyMessageDelayed(0, 5);
                }
            }
        };


        thread2 = new HandlerThread("Sender");
        thread2.start();

        sender = new Handler(thread2.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                synchronized (value) {

                    Intent sendIntent = new Intent(SEND_GRAPH_DATA_BROADCAST);
                    sendIntent.putExtra(EXTRA_VALUE, value);
                    sendBroadcast(sendIntent);
                    for(int i=0;i<value.size();i++) {
                        Log.i(TAG, "[SEND] : (" + i + ") [" + value.get(i) + "]");
                    }
                    sender.sendEmptyMessageDelayed(0, 40);
                    value.clear();
                }
            }
        };

    }

    @OnClick(R.id.bt_generate)
    void onClickGenerateData(){
        Log.d(TAG, "onClickGenerateData!");
        generator.sendEmptyMessage(0);
        sender.sendEmptyMessageDelayed(0, 50);
    }

    @OnClick(R.id.bt_show)
    void onClickShowGraph(){
        Log.d(TAG, "onClickShowGraph!");
        startActivity(new Intent(this, GraphActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isFinishing()) {
            generator.removeCallbacksAndMessages(null);
            generator = null;
            sender.removeCallbacksAndMessages(null);
            sender = null;
        }
    }
}
