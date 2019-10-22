package com.example.asus.linechartdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    //数据列表和坐标文本列表
    private int[] mDataList;
    private int mMax;
    private String[] mHorizontalAxis;

    //折线图中点的集合
    private List<Dot> mDots=new ArrayList<Dot>();

    //每个点可支配的宽度
    private int mStep;

    //文本画笔
    private Paint mAxisPaint;
    //折线画笔
    private Paint mLinePaint;
    //点的画笔
    private Paint mDotPaint;
    //填充色的画笔
    private Paint mGrandientPaint;
    //点之间的连线路径
    private Path mPath;

    private Path mGrandientPath;
    //绘制文本的矩形
    private Rect mTextRect;

    //坐标与文本之间的距离
    private int mGap;
    //点的半径
    private int mRadius;
    //点的颜色
    private int mNormalDotColor;
    //点击时点的颜色
    private  int mSelectedDotColor;
    //线的颜色
    private int mLineColor;
    //渐变色的颜色
    private int[] DEFAULT_GRADIENT_COLORS={Color.RED,Color.YELLOW};
    //点击半径
    private int mClickRadius;
    //记录点击了哪个点
    private int mSelectedDotIndex=-1;

    public void setmDataList(int[] mDataList,int max) {
        this.mDataList = mDataList;
        this.mMax=max;
    }

    public void setmHorizontalAxis(String[] mHorizontalAxis) {
        this.mHorizontalAxis = mHorizontalAxis;
    }

    public LineChartView(Context context) {
        this(context,null);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //获取TypeArray对象
        TypedArray typedValue=context.obtainStyledAttributes(attrs,R.styleable.LineChartView);
        //获取线的颜色
        mLineColor=typedValue.getColor(R.styleable.LineChartView_line_color,Color.BLACK);
        mNormalDotColor=typedValue.getColor(R.styleable.LineChartView_dot_normal_color,Color.BLACK);
        mSelectedDotColor=typedValue.getColor(R.styleable.LineChartView_dot_selected_color,Color.RED);
        typedValue.recycle();

        initPaint();

        mPath=new Path();
        mGrandientPath=new Path();

        mRadius=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,4,getResources().getDisplayMetrics());
        mClickRadius=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,getResources().getDisplayMetrics());
        mTextRect=new Rect();
        mGap=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());

    }
    private void initPaint(){
        mAxisPaint=new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mDotPaint=new Paint();
        mDotPaint.setAntiAlias(true);

        mLinePaint=new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(mLineColor);

        mGrandientPaint=new Paint();
        mGrandientPaint.setAntiAlias(true);
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        canvas.drawPath(mPath,mLinePaint);
        canvas.drawPath(mGrandientPath,mGrandientPaint);
        for(int i=0;i<mDots.size();i++){
            //绘制坐标文本
            String axis=mHorizontalAxis[i];
            int x=getPaddingLeft()+i*mStep;
            int y=getHeight()-getPaddingBottom();
            canvas.drawText(axis,x,y,mAxisPaint);
            //绘制点
            Dot dot=mDots.get(i);
            if(i==mSelectedDotIndex){
                mDotPaint.setColor(mSelectedDotColor);
                float valueTextX=dot.x;
                float valueTextY=dot.y-mRadius-mGap;
                canvas.drawText(String.valueOf(mDataList[i]),valueTextX,valueTextY,mAxisPaint);
            }else{
                mDotPaint.setColor(mNormalDotColor);
            }
            canvas.drawCircle(dot.x,dot.y,mRadius,mDotPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
        //清空点的集合
        mDots.clear();
        //去除Padding,计算绘制区域的宽高
        int width=w-getPaddingLeft()-getPaddingRight();
        int height=h-getPaddingTop()-getPaddingBottom();
        //根据点的个数平分宽度
        mStep=width/(mDataList.length-1);
        //通过坐标文本画笔计算绘制x轴第一个坐标文本占据的矩形边界，
        //这里主要获取其高度，为计算maxBarHeight提供数据，maxBarHeight为折线图最大高度
        mAxisPaint.getTextBounds(mHorizontalAxis[0],0,mHorizontalAxis[0].length(),mTextRect);
        //计算折线图高度的最大像素大小，mTextRect.height为底部x轴坐标文本的高度，
        //mGap为坐标文本与折线之间间隔大小的变量
        int maxBarHeight=height-mTextRect.height()-mGap;
        //计算折线图最大高度与最大数据值的比值
        float heightRatio=maxBarHeight/mMax;
        //遍历所有的点
        for(int i=0;i<mDataList.length;i++){
            //初始化对应位置的点
            Dot dot=new Dot();
            dot.value=mDataList[i];
            dot.transformedValue=(int)(dot.value*heightRatio);
            dot.x=mStep*i+getPaddingLeft();
            dot.y=getPaddingTop()+maxBarHeight-dot.transformedValue;
            //当是第一个点时，将路径移动到该点
            if(i==0){
                mPath.moveTo(dot.x,dot.y);
                mGrandientPath.moveTo(dot.x,dot.y);
            }else{
                //路径连线到点dot
                mPath.lineTo(dot.x,dot.y);
                mGrandientPath.lineTo(dot.x,dot.y);
            }
            if(i==mDataList.length-1){
                int bottom=getPaddingTop()+maxBarHeight;
                //将渐变路径连接到最后一个点在竖直方向的最低点
                mGrandientPath.lineTo(dot.x,bottom);

                Dot firstDot=mDots.get(0);
                //连接到第一个点在竖直方向的最低点
                mGrandientPath.lineTo(firstDot.x,bottom);
                mGrandientPath.lineTo(firstDot.x,firstDot.y);
            }
            mDots.add(dot);
        }
        //LinearGrandient
        Shader shader=new LinearGradient(0,0,0,getHeight(),DEFAULT_GRADIENT_COLORS,null,Shader.TileMode.CLAMP);
        mGrandientPaint.setShader(shader);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mSelectedDotIndex=getClickDotIndex(event.getX(),event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //重置
                mSelectedDotIndex=-1;
                invalidate();
                break;
        }
        return true;
    }
    private int getClickDotIndex(float x,float y){
        int index=-1;
        for(int i=0;i<mDots.size();i++){
            Dot dot=mDots.get(i);
            int left=dot.x-mClickRadius;
            int top=dot.y-mClickRadius;
            int right=dot.x+mClickRadius;
            int bottom=dot.y+mClickRadius;
            if(x>left&&x<right&&y>top&&y<bottom){
                index=i;
                break;
            }
        }
        return index;
    }
}
