package com.example.asus.mykougoumusic;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import static com.example.asus.mykougoumusic.MyMusic.getmusicName1;

public class disc extends Activity implements OnGestureListener {
    private ObjectAnimator discObjectAnimator, neddleObjectAnimator;
    private GestureDetector gestureDetector;
    private TextView tv1;
    private TextView tv2;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disc);
        gestureDetector = new GestureDetector(this, this);

        tv1 = (TextView) findViewById(R.id.textView11);
        tv2 = (TextView) findViewById(R.id.textView22);

        //最外部的半透明边线
        OvalShape ovalShape0 = new OvalShape();
        ShapeDrawable drawable0 = new ShapeDrawable(ovalShape0);
        drawable0.getPaint().setColor(0x10000000);
        drawable0.getPaint().setStyle(Paint.Style.FILL);
        drawable0.getPaint().setAntiAlias(true);

        //黑色唱片边框
        RoundedBitmapDrawable drawable1 = RoundedBitmapDrawableFactory.create(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.disc));
        drawable1.setCircular(true);
        drawable1.setAntiAlias(true);

        //内层黑色边线
        OvalShape ovalShape2 = new OvalShape();
        ShapeDrawable drawable2 = new ShapeDrawable(ovalShape2);
        drawable2.getPaint().setColor(Color.BLACK);
        drawable2.getPaint().setStyle(Paint.Style.FILL);
        drawable2.getPaint().setAntiAlias(true);

        //最里面的图像
        RoundedBitmapDrawable drawable3 = RoundedBitmapDrawableFactory.create(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.icon));
        drawable3.setCircular(true);
        drawable3.setAntiAlias(true);

        Drawable[] layers = new Drawable[4];
        layers[0] = drawable0;
        layers[1] = drawable1;
        layers[2] = drawable2;
        layers[3] = drawable3;

        LayerDrawable layerDrawable = new LayerDrawable(layers);

        int width = 10;
        //针对每一个图层进行填充，使得各个圆环之间相互有间隔，否则就重合成一个了。
        //layerDrawable.setLayerInset(0, width, width, width, width);
        layerDrawable.setLayerInset(1, width, width, width, width);
        layerDrawable.setLayerInset(2, width * 11, width * 11, width * 11, width * 11);
        layerDrawable.setLayerInset(3, width * 12, width * 12, width * 12, width * 12);

        final View discView = findViewById(R.id.myView);
        discView.setBackgroundDrawable(layerDrawable);

        ImageView needleImage = (ImageView) findViewById(R.id.needle);

        discObjectAnimator = ObjectAnimator.ofFloat(discView, "rotation", 0, 360);
        discObjectAnimator.setDuration(20000);
        //使ObjectAnimator动画匀速平滑旋转
        discObjectAnimator.setInterpolator(new LinearInterpolator());
        //无限循环旋转
        discObjectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //discObjectAnimator.setRepeatMode(ValueAnimator.INFINITE);


        neddleObjectAnimator = ObjectAnimator.ofFloat(needleImage, "rotation", 0, 25);
        needleImage.setPivotX(0);
        needleImage.setPivotY(0);
        neddleObjectAnimator.setDuration(800);
        neddleObjectAnimator.setInterpolator(new LinearInterpolator());

//---------------------------------------------------------
        if (MyMusic.a2() == 1) {
            discObjectAnimator.start();//------------------------------开始
            neddleObjectAnimator.start();
            disc.this.tv1.setText(getmusicName1 ());
            //disc.this.tv2.setText(getmusicArtist1 ());
            Log.e("转转", "开始");
        } else {
            discObjectAnimator.cancel();//----------------------------结束
            neddleObjectAnimator.reverse();
            Log.e("转转", "结束");
        }
    }

    //下面是滑屏效果------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return gestureDetector.onTouchEvent(event);
    }

    //    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        if (arg1.getX() - arg0.getX() < 150) {
            Log.e("在歌曲列表这边划了一下", "");
            Intent intent8 = new Intent(disc.this, MyMusic.class);
            startActivity(intent8);
            overridePendingTransition(R.anim.to_left_enter1, R.anim.to_left1_exit);
            Toast.makeText(getApplicationContext(), "界面", Toast.LENGTH_SHORT).show();
            finish();
        }
        return false;
    }
    //------------------------

}

