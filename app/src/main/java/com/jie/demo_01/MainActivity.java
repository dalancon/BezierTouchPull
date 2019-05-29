package com.jie.demo_01;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private float mTouchStartY;
    private static final float TOUCH_MOVE_MAX_Y = 600; //y轴移动的最大值
    private MyTouchPullView touchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        touchView = (MyTouchPullView)findViewById(R.id.touchView);
        findViewById(R.id.ll_mainLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int actionMasked = event.getActionMasked();
                switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchStartY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float y = event.getY();
                        if (y >= mTouchStartY) {  //表示向下移动
                            float moveSize = y - mTouchStartY;
                            float progress = moveSize > TOUCH_MOVE_MAX_Y ?
                                    1 : moveSize / TOUCH_MOVE_MAX_Y;   //计算进度值
                            touchView.setProgress(progress);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        touchView.release();
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
    }
}
