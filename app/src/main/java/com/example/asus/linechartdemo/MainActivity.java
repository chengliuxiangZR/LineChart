package com.example.asus.linechartdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String[] HORIZONTAL_AXIS={"1","2","3","4","5","6","7","8","9","10","11","12"};
    private static final int[] DATA={12,24,45,56,89,70,49,22,23,10,12,3};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LineChartView lineChartView=(LineChartView)findViewById(R.id.line_chart_view);
        lineChartView.setmHorizontalAxis(HORIZONTAL_AXIS);
        lineChartView.setmDataList(DATA,89);
    }
}
