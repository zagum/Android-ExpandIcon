package com.github.zagum.expandiconview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.github.zagum.expandicon.ExpandIconView;

public class SampleActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

  private ExpandIconView expandIconView;
  private View container;
  private GestureDetector gestureDetector;
  private View swipeDetectionView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample);
    expandIconView = (ExpandIconView) findViewById(R.id.expand_icon);
    container = findViewById(R.id.container);
    container.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        expandIconView.switchState();
      }
    });

    setUpSlidingContainer();
  }

  private void setUpSlidingContainer() {
    gestureDetector = new GestureDetector(this, this);
    gestureDetector.setIsLongpressEnabled(false);

    swipeDetectionView = findViewById(R.id.swipe_detector);
    swipeDetectionView.setClickable(true);
    swipeDetectionView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });
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
    Log.d("speed", distanceY+"");
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return false;
  }
}
