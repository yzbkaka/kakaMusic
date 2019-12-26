package com.yzbkaka.kakamusic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yzbkaka.kakamusic.R;


public class SideBar extends View{

    private OnTouchingLetterChangedListener onListener;

    /**
     * 26个字母的索引和#
     */
    public static String[] sections = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#" };

    private Context context;

    /**
     * 当前选中的位置
     */
    private int selected = -1;

    private int oldChoose;

    private Paint indexPaint = new Paint();

    private Paint circlePaint = new Paint();

    private Paint textPaint = new Paint();

    private float radius = 0;   //显示索引圆形view半径

    private Rect bounds = new Rect(); //单个索引字母边界

    private float width;    //view宽

    private float height;   //view高

    /**
     * 每一个字母的高度
     */
    private float singleHeight;

    private float curY = 0; //当前y坐标

    private int accentColor;


    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }


    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }


    public SideBar(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * 进行绘制
     */
    private void init(){
        int defaultColor = 0xFFFA7298;
        int[] attrsArray = {R.attr.colorAccent};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        accentColor = typedArray.getColor(0, defaultColor);  //获取主题颜色
        typedArray.recycle();  //释放资源

        //设置画预览圆形的画笔
        circlePaint.setColor(accentColor);
        circlePaint.setAntiAlias(true);  //设置抗锯齿
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);  //既绘制轮廓也绘制内容
        radius = dp2px(30);

        //设置画预览圆形中索引字母的画笔
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(radius * 1.2f );
    }


    /**
     * 确定元素位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        height = getHeight(); //获取对应高度
        width = getWidth();   //获取对应宽度
        singleHeight = height / sections.length; //获取每一个字母的高度
    }


    /**
     * 绘制元素
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < sections.length; i++) {
            int textSize = (int) (singleHeight * 0.7f );
            //设置右边索引的画笔
            indexPaint.setColor(Color.GRAY);
            indexPaint.setAntiAlias(true);
            indexPaint.setTextSize(textSize);

            //索引被选中的状态
            if (i == selected) {
                indexPaint.setColor(accentColor);
                indexPaint.setFakeBoldText(true);  //字体加粗

                Rect rect = new Rect();
                textPaint.getTextBounds(sections[i], 0, 1, rect);  //获得文字的宽度信息并赋给了rect

                canvas.drawCircle((width-singleHeight)/2.0f,curY,radius,circlePaint);  //画出预览圆；圆心x坐标、圆心y坐标、半径和画笔

                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(sections[i],(width-singleHeight)/2.0f,curY + rect.height()/2.0f,textPaint);  //画出预览的字母；显示的文字、x坐标、y坐标和画笔
            }

            indexPaint.getTextBounds(sections[i], 0, 1, bounds);
            float xPos = (width - singleHeight) + singleHeight / 2.0f - indexPaint.measureText(sections[i]) / 2.0f;
            float yPos = singleHeight * i + (singleHeight + bounds.height())/2.0f ;
            canvas.drawText(sections[i], xPos, yPos, indexPaint);  //画出左边的索引
            indexPaint.reset();
        }
    }


    /**
     * 事件的分发
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        curY = event.getY(); // 点击y坐标
        oldChoose = selected;
        int preCount = (int) (curY / (getHeight() / sections.length));  //点击该点前面的索引个数
        switch (action) {
            case MotionEvent.ACTION_UP:
                selected = -1;
                invalidate();  //UI进行刷新
                break;
            case  MotionEvent.ACTION_DOWN:
                if (event.getX() < width - singleHeight) return false;  //点击其他的范围；不能break
            default:
                if (oldChoose != preCount) {
                    if (preCount >= 0 && preCount < sections.length) {
                        if (onListener != null) {
                            onListener.onTouchingLetterChanged(sections[preCount]);
                        }
                        selected = preCount;
                    }
                }
                curY = (curY < radius) ? radius : curY;
                curY = (curY + radius > height) ? (height - radius) : curY;
                invalidate();
                break;
        }
        return true;
    }


    public void setOnListener(OnTouchingLetterChangedListener onListener) {
        this.onListener = onListener;
    }


    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String letter);
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
