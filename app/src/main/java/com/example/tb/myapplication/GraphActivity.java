package com.example.tb.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class GraphActivity extends AppCompatActivity {
    private static final String TAG = "GraphActivity";

    private static final float chartWidth=640f;

    @BindView(R.id.chart)
    LineChartView chart;
//    String[] date = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10","11","12","13","14","15","16","17","18","19","20"};//X轴的标注
//    int[] score = {25, 22, 18, 16, 15, 30, 22, 35, 37, 10,15,18,20,24,25,26,27,28,29,30};//图表的数据点
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
//    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private float mFirstX,mLastX;
    BroadcastReceiver mReceiver;
    IntentFilter intentfilter;

    List<Line> lines = new ArrayList<Line>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        ButterKnife.bind(this);

        chart.setBackgroundColor(Color.BLACK);

        intentfilter = new IntentFilter();
        intentfilter.addAction(MainActivity.SEND_GRAPH_DATA_BROADCAST); //동적 리시버 구현
        mReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Integer> value = (List<Integer>) intent.getSerializableExtra(MainActivity.EXTRA_VALUE);
                for(int i=0;i<value.size();i++) {
                    Log.i(TAG, "[RECV] : (" + i + ") [" + value.get(i) + "]");
                }

//                synchronized (mPointValues) {
                    int addedLength = value.size();
                    for (int i = 0; i < value.size(); i++) {
                        mPointValues.add(new PointValue(i, value.get(i)));
                    }
                    if (mPointValues.size() > chartWidth) {
                        int removeLength = (int) (mPointValues.size() - chartWidth);
                        for (int i = 0; i < removeLength; i++) {
                            mPointValues.remove(0);
                        }
                    }

                    int min = Integer.MAX_VALUE;
                    int max = Integer.MIN_VALUE;

                    for (int i = 0; i < mPointValues.size(); i++) {
                        mPointValues.get(i).set(i, mPointValues.get(i).getY());
                        Log.i(TAG, "[DRAW] : (" + i + ") [" + mPointValues.get(i).getX() + "][" + mPointValues.get(i).getY() + "]");
                        if(mPointValues.get(i).getY() > max) {
                            max = (int) mPointValues.get(i).getY();
                        }

                        if(mPointValues.get(i).getY() < min) {
                            min = (int) mPointValues.get(i).getY();
                        }

                    }

                    for (int i = 0; i < mPointValues.size(); i++) {
                        Log.i(TAG, "[DRAW2] : (" + i + ") [" + mPointValues.get(i).getX() + "][" + mPointValues.get(i).getY() + "]");
                    }
                    Log.i(TAG, "[MIN MAX] : [" + min + "][" + max+ "]");
                    drawGraph(min, max);
//                }
            }
        };

    }


    void drawGraph(int minHeight, int maxHeight) {
        //        getAxisXLables();
//        getAxisPoints();
        Log.i(TAG, "[DRAW] Start");
        //In most cased you can call data model methods in builder-pattern-like manner.
//        Line line = new Line(mPointValues).setColor(Color.BLUE).setCubic(true).setHasLabels(true).setStrokeWidth(5).setShape(ValueShape.CIRCLE);
//        Line line = new Line(mPointValues).setColor(Color.RED).setStrokeWidth(1);
        Line line = new Line(mPointValues);
        line.setColor(ChartUtils.COLOR_RED);
        line.setHasPoints(false);// too many values so don't draw points.
        lines.clear();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        //三个同时设置，才能设置标签背景色
//        data.setValueLabelBackgroundEnabled(true);
//        data.setValueLabelBackgroundAuto(false);
//        data.setValueLabelBackgroundColor(Color.BLACK);



/*        //坐标轴
        Axis axisX = new Axis(); //X轴
//        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLACK);  //设置字体颜色
        //axisX.setName("date");  //表格名称
        axisX.setTextSize(20);//设置字体大小
//        axisX.setMaxLabelChars(8); //
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线
        axisX.setHasSeparationLine(false);//设置标签跟图表之间的轴线
        axisX.setLineColor(Color.YELLOW);
//        axisX.generateAxisFromRange(0,0,1);//从已知当中截取
        data.setAxisXBottom(axisX); //x 轴在底部

        //设置上下两个轴线，为了防止绘制的曲线被遮挡，可以留出空隙==========================================
        Axis axisXTop = new Axis(); //X轴
        axisXTop.setTextColor(Color.TRANSPARENT);  //设置字体颜色
//        axisXTop.setTextSize(20);//设置字体大小
//        axisXTop.setValues(mAxisXValues);  //填充X轴的坐标名称
//        axisXTop.setHasLines(true); //x 轴分割线
//        axisXTop.setHasSeparationLine(false);//设置标签跟图表之间的轴线
//        axisXTop.setLineColor(Color.RED);
        data.setAxisXTop(axisXTop); //x 轴在底部*/


        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
//        Axis axisY = new Axis();  //Y轴
//        axisY.setName("");//y轴标注
//        axisY.setTextSize(20);//设置字体大小
//        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边

//        chart.setValueTouchEnabled(false);
        chart.setZoomEnabled(false);
//        chart.setScrollEnabled(false);
//        chart.setInteractive(false);
//        chart.setZoomType(ZoomType.HORIZONTAL);
//        chart.setMaxZoom((float) 2);//最大方法比例
//        chart.setZoomLevel(0,0,2);
//        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
//
        chart.setLineChartData(data);

        //viewport必须设置在setLineChartData后面，设置一个当前viewport，再设置一个maxviewport，就可以实现滚动，高度要设置数据的上下限
        chart.setViewportCalculationEnabled(false);
        final Viewport v = new Viewport(chart.getMaximumViewport());
//        Log.e(TAG, "onCreate: "+v.left+"#"+v.top+"#"+v.right+"$"+v.bottom );
        v.left = 0;
        v.right= chartWidth;
        v.top = maxHeight + 100;
        v.bottom = minHeight - 100;
        chart.setCurrentViewport(v);

//        Log.e(TAG, "onCreate: "+v.left+"#"+v.top+"#"+v.right+"$"+v.bottom );

        final Viewport maxV=new Viewport(chart.getMaximumViewport());
        maxV.left=0;
        maxV.right= chartWidth;
        maxV.top = maxHeight + 100;
        maxV.bottom = minHeight - 100;
        chart.setMaximumViewport(maxV);

        Log.i(TAG, "[DRAW] Complete");
        final Rect rect=chart.getChartComputator().getContentRectMinusAllMargins();

//        chart.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                float fx=chart.getChartComputator().computeRawDistanceX(5);
//                float fy=chart.getChartComputator().computeRawDistanceY(100);
//                Rect r=chart.getChartComputator().getContentRectMinusAllMargins();
//                float x=motionEvent.getX()/(r.width()/v.width());
//                float y=motionEvent.getY()/(r.height()/v.height());
//                chart.moveTo(x,y);
//
//                switch (motionEvent.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        mFirstX=motionEvent.getX();
//                        mLastX=motionEvent.getX();
////                        Log.e(TAG, "onTouch: "+mFirstX );
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        float totalDeltaX=motionEvent.getX()-mLastX;
//                        mLastX=motionEvent.getX();
//                        float realX=v.width()*totalDeltaX/rect.width();
//
//                        Log.e(TAG, "onTouch: "+realX +"#"+totalDeltaX+"$$$"+rect.width()+"$"+v.width()+"###");
//                        Log.e(TAG, "onTouch:before... "+v.left+"#"+v.top+"$"+v.right+"$"+v.bottom );
//                        Viewport vTemp=new Viewport(v);
//                        vTemp.left += -realX;
//                        vTemp.right = vTemp.left+chartWidth;
//                        if(vTemp.left<0){
//                            vTemp.left=0;
//                            vTemp.right=chartWidth;
//                        }
//                        if(vTemp.left>score.length-1-chartWidth){
//                            vTemp.left=score.length-1-chartWidth;
//                            vTemp.right=score.length-1;
//                        }
//                        if(vTemp.right>score.length-1){
//                            vTemp.right=score.length-1;
//                            vTemp.left=score.length-1-chartWidth;
//                        }
//                        if(vTemp.right-vTemp.left!=chartWidth){
//                            break;
//                        }
//                        v.set(vTemp);
//                        chart.setMaximumViewport(v);
//                        chart.setCurrentViewport(v);
//                        Log.e(TAG, "onTouch:after... "+v.left+"#"+v.top+"$"+v.right+"$"+v.bottom );
//                        break;
//                    case MotionEvent.ACTION_UP:
//                    default:
//                        break;
//                }
//                return true;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentfilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**
     * 设置X 轴的显示
     */
//    private void getAxisXLables() {
//        for (int i = 0; i < date.length; i++) {
//            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
//        }
//    }

    /**
     * 图表的每个点的显示
     */
//    private void getAxisPoints() {
//        for (int i = 0; i < score.length; i++) {
//            mPointValues.add(new PointValue(i, score[i]));
//        }
//    }

}
