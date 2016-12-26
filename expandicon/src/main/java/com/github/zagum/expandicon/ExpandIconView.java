package com.github.zagum.expandicon;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class ExpandIconView extends View {

  private static final float MORE_STATE_ALPHA = -45f;
  private static final float LESS_STATE_ALPHA = 45f;
  private static final float DELTA_ALPHA = 90f;
  private static final float THICKNESS_PROPORTION = 5f / 36f;
  private static final long DEFAULT_ANIMATION_DURATION = 150;

  @IntDef({
      MORE,
      LESS,
      INTERMEDIATE
  })

  @Retention(RetentionPolicy.SOURCE)

  public @interface State {
  }

  public static final int MORE = 0;
  public static final int LESS = 1;
  private static final int INTERMEDIATE = 2;

  @State
  private int state;
  private int width;
  private int height;
  private int arrowWidth;
  private float alpha = MORE_STATE_ALPHA;
  private float centerTranslation = 0f;
  private float fraction = 0f;
  private float animationSpeed;

  private boolean roundedCorners = false;
  private boolean switchColor = false;
  private long animationDuration = DEFAULT_ANIMATION_DURATION;
  private int color = Color.BLACK;
  private int colorMore = Color.BLACK;
  private int colorLess = Color.RED;

  private Paint paint;
  private Point left;
  private Point right;
  private Point center;
  private final Path path = new Path();

  public void switchState() {
    switchState(true);
  }

  public void switchState(boolean animate) {
    if (state == MORE) {
      setState(LESS, animate);
    } else if (state == LESS) {
      setState(MORE, animate);
    } else {
      setState(getFinalStateByFraction(), animate);
    }
  }

  public void setState(@State int state, boolean animate) {
    this.state = state;
    if (state == MORE) {
      fraction = 0f;
    } else if (state == LESS) {
      fraction = 1f;
    }
    updateArrow(animate);
  }

  /**
   * @see #MORE = 0
   * @see #LESS = 1
   */
  public void setFraction(float fraction, boolean animate) {
    if (fraction < 0f || fraction > 1f) {
      throw new IllegalArgumentException("Progress value must be from 0 to 100");
    }
    this.fraction = fraction;
    if (fraction == 0f) {
      state = MORE;
    } else if (fraction == 1f) {
      state = LESS;
    } else {
      state = INTERMEDIATE;
    }
    updateArrow(animate);
  }

  public ExpandIconView(Context context) {
    super(context);
    init();
  }

  public ExpandIconView(Context context, AttributeSet attrs) {
    super(context, attrs);
    readAttributes(attrs);
    init();
  }

  public ExpandIconView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    readAttributes(attrs);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public ExpandIconView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    readAttributes(attrs);
    init();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.translate(0, centerTranslation);
    canvas.drawPath(path, paint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    width = getMeasuredWidth();
    height = getMeasuredHeight();

    arrowWidth = height >= width ? width : height;

    float thickness = (int) (arrowWidth * THICKNESS_PROPORTION);
    paint.setStrokeWidth(thickness);

    center.set(width / 2, height / 2);
    left.set(center.x - arrowWidth / 2, center.y);
    right.set(center.x + arrowWidth / 2, center.y);
    updateArrowPath();
  }

  private void init() {
    paint = new Paint(ANTI_ALIAS_FLAG);
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setDither(true);
    if (roundedCorners) {
      paint.setStrokeJoin(Paint.Join.ROUND);
      paint.setStrokeCap(Paint.Cap.ROUND);
    }

    left = new Point();
    right = new Point();
    center = new Point();

    animationSpeed = DELTA_ALPHA / animationDuration;

    setState(MORE, false);
  }

  private void readAttributes(AttributeSet attrs) {
    TypedArray array = getContext().getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.ExpandIconView,
        0, 0);

    try {
      roundedCorners = array.getBoolean(R.styleable.ExpandIconView_roundedCorners, false);
      switchColor = array.getBoolean(R.styleable.ExpandIconView_switchColor, false);
      color = array.getColor(R.styleable.ExpandIconView_color, Color.BLACK);
      colorMore = array.getColor(R.styleable.ExpandIconView_colorMore, Color.BLACK);
      colorLess = array.getColor(R.styleable.ExpandIconView_colorLess, Color.BLACK);
      animationDuration = array.getInteger(R.styleable.ExpandIconView_animationDuration, (int) DEFAULT_ANIMATION_DURATION);
    } finally {
      array.recycle();
    }
  }

  private void updateArrow(boolean animate) {
    float toAlpha = MORE_STATE_ALPHA + (fraction * DELTA_ALPHA);
    if (animate) {
      animateArrow(toAlpha);
    } else {
      alpha = toAlpha;
      if (switchColor) {
        updateColor(new ArgbEvaluator());
      }
      updateArrowPath();
      invalidate();
    }
  }

  private void updateArrowPath() {
    path.reset();
    if (left != null && right != null) {
      Point currLeft = rotate(left, -alpha);
      Point currRight = rotate(right, alpha);
      centerTranslation = (center.y - currLeft.y) / 2;
      path.moveTo(currLeft.x, currLeft.y);
      path.lineTo(center.x, center.y);
      path.lineTo(currRight.x, currRight.y);
    }
  }

  private void animateArrow(float toAlpha) {
    final ValueAnimator animator = ValueAnimator.ofFloat(alpha, toAlpha);
    final ArgbEvaluator colorEvaluator = new ArgbEvaluator();
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        alpha = (float) valueAnimator.getAnimatedValue();
        updateArrowPath();
        if (switchColor) {
          updateColor(colorEvaluator);
        }
        postInvalidateOnAnimation();
      }
    });
    animator.setInterpolator(new DecelerateInterpolator());
    animator.setDuration(calculateAnimationDuration(toAlpha));
    animator.start();
  }

  private void updateColor(ArgbEvaluator colorEvaluator) {
    color = (int) colorEvaluator.evaluate((alpha + 45f) / 90f, colorMore, colorLess);
    paint.setColor(color);
  }

  private long calculateAnimationDuration(float toAlpha) {
    return (long) (Math.abs(toAlpha - alpha) / animationSpeed);
  }

  /**
   * X = x0 + (x - x0) * cos(a) - (y - y0) * sin(a);
   * Y = y0 + (y - y0) * cos(a) + (x - x0) * sin(a);
   */
  private Point rotate(Point startPosition, double degrees) {
    double angle = Math.toRadians(degrees);
    int x = (int) (center.x + (startPosition.x - center.x) * Math.cos(angle) -
        (startPosition.y - center.y) * Math.sin(angle));

    int y = (int) (center.y + (startPosition.x - center.x) * Math.sin(angle) +
        (startPosition.y - center.y) * Math.cos(angle));
    return new Point(x, y);
  }

  @State
  private int getFinalStateByFraction() {
    if (fraction <= .5f) {
      return MORE;
    } else {
      return LESS;
    }
  }
}
