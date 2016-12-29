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
  private static final float PADDING_PROPORTION = 4f / 24f;
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
  private boolean useDefaultPadding;

  private boolean roundedCorners = false;
  private boolean switchColor = false;
  private long animationDuration = DEFAULT_ANIMATION_DURATION;
  private int color = Color.BLACK;
  private int colorMore = Color.BLACK;
  private int colorLess = Color.RED;
  private int padding;

  private Paint paint;
  private Point left;
  private Point right;
  private Point center;
  private final Path path = new Path();
  private ValueAnimator arrowAnimator;

  public void switchState() {
    switchState(true);
  }

  /**
   * Changes state and updates view
   *
   * @param animate Indicates thaw state will be changed with animation or not
   */
  public void switchState(boolean animate) {
    if (state == MORE) {
      setState(LESS, animate);
    } else if (state == LESS) {
      setState(MORE, animate);
    } else {
      setState(getFinalStateByFraction(), animate);
    }
  }

  /**
   * Set one of two states and updates view
   *
   * @param state {@link #MORE} or {@link #LESS}
   * @param animate Indicates thaw state will be changed with animation or not
   * @throws IllegalArgumentException if {@param state} is invalid
   */
  public void setState(@State int state, boolean animate) {
    this.state = state;
    if (state == MORE) {
      fraction = 0f;
    } else if (state == LESS) {
      fraction = 1f;
    } else {
      throw new IllegalArgumentException("Unknown state, must be one of STATE_MORE = 0,  STATE_LESS = 1");
    }
    updateArrow(animate);
  }

  /**
   * Set current fraction for arrow and updates view
   *
   * @param fraction Must be value from 0f to 1f {@link #MORE} state value is 0f, {@link #LESS}
   * state value is 1f
   * @throws IllegalArgumentException if fraction is less than 0f or more than 1f
   */
  public void setFraction(float fraction, boolean animate) {
    if (fraction < 0f || fraction > 1f) {
      throw new IllegalArgumentException("Fraction value must be from 0 to 1f, fraction=" + fraction);
    }
    if (this.fraction == fraction) return;
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
    calculateArrowMetrics();
    updateArrowPath();
  }

  private void readAttributes(AttributeSet attrs) {
    TypedArray array = getContext().getTheme().obtainStyledAttributes(
        attrs,
        R.styleable.ExpandIconView,
        0, 0);

    try {
      roundedCorners = array.getBoolean(R.styleable.ExpandIconView_eiv_roundedCorners, false);
      switchColor = array.getBoolean(R.styleable.ExpandIconView_eiv_switchColor, false);
      color = array.getColor(R.styleable.ExpandIconView_eiv_color, Color.BLACK);
      colorMore = array.getColor(R.styleable.ExpandIconView_eiv_colorMore, Color.BLACK);
      colorLess = array.getColor(R.styleable.ExpandIconView_eiv_colorLess, Color.BLACK);
      animationDuration = array.getInteger(R.styleable.ExpandIconView_eiv_animationDuration, (int) DEFAULT_ANIMATION_DURATION);
      padding = array.getDimensionPixelSize(R.styleable.ExpandIconView_eiv_padding, -1);
      if (padding == -1) useDefaultPadding = true;
    } finally {
      array.recycle();
    }
  }

  private void calculateArrowMetrics() {
    int arrowMaxHeight = height - 2 * padding;
    arrowWidth = width - 2 * padding;
    arrowWidth = arrowMaxHeight >= arrowWidth ? arrowWidth : arrowMaxHeight;

    if (useDefaultPadding) {
      padding = (int) (PADDING_PROPORTION * width);
    }

    float thickness = (int) (arrowWidth * THICKNESS_PROPORTION);
    paint.setStrokeWidth(thickness);

    center.set(width / 2, height / 2);
    left.set(center.x - arrowWidth / 2, center.y);
    right.set(center.x + arrowWidth / 2, center.y);
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

  private void updateArrow(boolean animate) {
    float toAlpha = MORE_STATE_ALPHA + (fraction * DELTA_ALPHA);
    if (animate) {
      animateArrow(toAlpha);
    } else {
      cancelAnimation();
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
    cancelAnimation();
    final ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    arrowAnimator = ValueAnimator.ofFloat(alpha, toAlpha);
    arrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        alpha = (float) valueAnimator.getAnimatedValue();
        updateArrowPath();
        if (switchColor) {
          updateColor(colorEvaluator);
        }
        postInvalidateOnAnimationCompat();
      }
    });
    arrowAnimator.setInterpolator(new DecelerateInterpolator());
    arrowAnimator.setDuration(calculateAnimationDuration(toAlpha));
    arrowAnimator.start();
  }

  private void cancelAnimation() {
    if (arrowAnimator != null && arrowAnimator.isRunning()) {
      arrowAnimator.cancel();
    }
  }

  private void updateColor(ArgbEvaluator colorEvaluator) {
    color = (int) colorEvaluator.evaluate((alpha + 45f) / 90f, colorMore, colorLess);
    paint.setColor(color);
  }

  private long calculateAnimationDuration(float toAlpha) {
    return (long) (Math.abs(toAlpha - alpha) / animationSpeed);
  }

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

  private void postInvalidateOnAnimationCompat() {
    final long fakeFrameTime = 10;
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      postInvalidateOnAnimation();
    } else {
      postInvalidateDelayed(fakeFrameTime);
    }
  }
}
