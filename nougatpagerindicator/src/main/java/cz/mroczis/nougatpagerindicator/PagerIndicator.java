package cz.mroczis.nougatpagerindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Simple pager indicator that is exact copy of pager indicator used in Android Nougat.
 * This implementation also adds animations from Android O.
 * <p>
 * Usage with ViewPager:
 * <ul>
 * <li>call {@link #setupWithViewPager(ViewPager)} when pager has its adapter attached
 * </li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * Manual usage:
 * <ul>
 * <li>{@link #setCurrentItem(int, boolean)} sets position of highlighted circle
 * </li>
 * </ul>
 * </p>
 * Created by Michal Mroček on 29.06.17.
 */

public class PagerIndicator extends View implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {

    /**
     * Default animation duration when user scrolling between 2 neighbouring dots is in progress.
     * The actual duration is 2 times longer (snapping to neighbour + jumping back no new/old position)
     * <p>
     * This value is in {@link #animate(int, int)} a bit changed to achieve smoother non-linear animations
     */
    private static final int ANIMATION_DURATION = 175;

    /**
     * Number of drawn dots when view is in edit mode and nobody specified count of dots
     * This helps developers to preview this component
     */
    private static final int EDIT_MODE_DOTS_COUNT = 3;


    /**
     * Total count of drawn dots
     * {@link #setDotsCount(int)}
     */
    private int mDotsCount;

    /**
     * Position of currently selected dot. It must be bigger or equal to 0 and lower than {@link #mDotsCount}
     */
    private int mCurrentItem;

    /**
     * Ratio in range of 0 to x. Where x - 1 indicated count of dots that are being overdrawn by rectangle.
     * For example when this is 1, then two neighbouring dots are connected
     */
    private float mGlueRatio = 0F;

    /**
     * Position of dot that is on the left side of rectangle that glues 2 dots
     */
    private int mGluedDotLeft = 0;

    /**
     * Position of dot that is on the right side of rectangle that glues 2 dots
     */
    private int mGluedDotRight = 0;

    /**
     * Radius of each dot
     */
    @Px
    private int mDotRadius;

    /**
     * Space between two dots
     */
    @Px
    private int mSpaceBetweenDots;

    /**
     * Total minimal width of this view
     */
    private int mTotalWidth;

    /**
     * Paint for currently selected dot, {@link #mCurrentItem} indicates position of that dot
     */
    private Paint mPaintActive;

    /**
     * Paint for all dots that are not currently selected
     */
    private Paint mPaintInactive;

    /**
     * Internal state of pager.
     * {@link PagerState#SCROLLING} - animation is now in progress
     * {@link PagerState#STILL} - nothing is being animated now
     */
    private PagerState mPagerState;

    /**
     * Last value of scroll - it is sum of page + offset and is used to determine scroll direction
     */
    private float mLastScrollValue;

    /**
     * ViewPager we are attached to, null when no pager is attached to this view
     */
    @Nullable
    private ViewPager mViewPager;

    public PagerIndicator(Context context) {
        super(context);
        init(null);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * Initializes the view with standard parameters or attributes
     *
     * @param attrs attributes for view
     */
    private void init(@Nullable AttributeSet attrs) {
        mPagerState = PagerState.STILL;

        mPaintActive = new Paint();
        mPaintActive.setAntiAlias(true);

        mPaintInactive = new Paint();
        mPaintInactive.setAntiAlias(true);

        int defActiveColor = ContextCompat.getColor(getContext(), R.color.pi_default_pager_active);
        int defInactiveColor = ContextCompat.getColor(getContext(), R.color.pi_default_pager_inactive);
        int defDotRadius = getResources().getDimensionPixelSize(R.dimen.pi_dot_radius);
        int defSpaceBetweenDots = getResources().getDimensionPixelSize(R.dimen.pi_dot_spacing);

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.PagerIndicator);

        if (array != null) {
            mPaintActive.setColor(array.getColor(R.styleable.PagerIndicator_npiActiveDotColor, defActiveColor));
            mPaintInactive.setColor(array.getColor(R.styleable.PagerIndicator_npiInactiveDotColor, defInactiveColor));

            mDotRadius = array.getDimensionPixelSize(R.styleable.PagerIndicator_npiDotRadius, defDotRadius);
            mSpaceBetweenDots = array.getDimensionPixelSize(R.styleable.PagerIndicator_npiDotSpacing, defSpaceBetweenDots);

            mDotsCount = array.getInt(R.styleable.PagerIndicator_npiDotsCount, isInEditMode() ? EDIT_MODE_DOTS_COUNT : 0);
            mCurrentItem = array.getInt(R.styleable.PagerIndicator_npiActiveDot, 0);

            array.recycle();
        } else {
            mPaintActive.setColor(defActiveColor);
            mPaintInactive.setColor(defInactiveColor);

            mDotRadius = defDotRadius;
            mSpaceBetweenDots = defSpaceBetweenDots;

            mDotsCount = isInEditMode() ? EDIT_MODE_DOTS_COUNT : 0;
            mCurrentItem = 0;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateTotalWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        calculateTotalWidth();
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int startPosition = (getWidth() - mTotalWidth) / 2 + mDotRadius;
        int centerY = getHeight() / 2;

        for (int i = 0; i < mDotsCount; ++i) {
            if (mGlueRatio == 0 || (i != mGluedDotLeft && i != mGluedDotRight)) { // drawing only dots that are not being animated now
                if (i == mCurrentItem && mGlueRatio == 0) {
                    canvas.drawCircle(startPosition + getDotOffset(i), centerY, mDotRadius, mPaintActive);
                } else if (i != mCurrentItem) {
                    canvas.drawCircle(startPosition + getDotOffset(i), centerY, mDotRadius, mPaintInactive);
                }
            }
        }

        if (mGlueRatio > 0F) {
            int offsetStart = getDotOffset(mGluedDotLeft);
            int offsetEnd = getDotOffset(mGluedDotRight);

            float movementCorrection = getMovementRatio(getDecimalPart(mGlueRatio)) * mDotRadius;
            float rectWidth = mGlueRatio * (mSpaceBetweenDots + 2 * mDotRadius); // total space between centers of both points

            if (mCurrentItem == mGluedDotLeft) {
                // scrolling from left to right
                canvas.drawCircle((int) (startPosition + offsetEnd - movementCorrection), centerY, mDotRadius, mPaintInactive); // inactive dot - simulating movementCorrection
                canvas.drawCircle(startPosition + offsetStart + rectWidth, centerY, mDotRadius, mPaintActive); // moving circle (right)
                canvas.drawRect(startPosition + offsetStart, centerY - mDotRadius, startPosition + offsetStart + rectWidth, centerY + mDotRadius, mPaintActive); // glue rect
                canvas.drawCircle(startPosition + offsetStart, centerY, mDotRadius, mPaintActive); // currently selected circle (left)
            } else {
                // scrolling from right to left
                canvas.drawCircle((int) (startPosition + offsetStart + movementCorrection), centerY, mDotRadius, mPaintInactive); // inactive dot - simulating movementCorrection
                canvas.drawCircle(startPosition + offsetEnd - rectWidth, centerY, mDotRadius, mPaintActive); // moving circle (left)
                canvas.drawRect(startPosition + offsetEnd - rectWidth, centerY - mDotRadius, startPosition + offsetEnd, centerY + mDotRadius, mPaintActive); // glue rect
                canvas.drawCircle(startPosition + offsetEnd, centerY, mDotRadius, mPaintActive); // currently selected circle (right)
            }
        }

    }

    /**
     * Returns decimal part of number
     * Example: input 3.1231523, output 0.1231523
     *
     * @param val some float number
     * @return just decimal part of given number
     */
    private float getDecimalPart(float val) {
        return val - (int) val;
    }

    /**
     * Calculates number in range of <0;1> which helps us determine position of sibling
     * dot while animating. This is used to achieve effect that another dot is getting closer
     * to another one.
     *
     * @param val input float number
     * @return ratio
     */
    private float getMovementRatio(float val) {
        return (float) Math.min(1, Math.pow(val, 2) + 0.075F);
    }

    /**
     * Calculates actual width of all elements that will be drawn
     */
    private void calculateTotalWidth() {
        if (mDotsCount > 0) {
            mTotalWidth = mDotsCount * mDotRadius * 2 + (mDotsCount - 1) * mSpaceBetweenDots;
        } else {
            mTotalWidth = 0;
        }
    }

    @Override
    public void onPageScrolled(int position, float offset, int positionOffsetPixels) {
        if (mPagerState == PagerState.STILL && offset != 0F) {
            // not animating or glue rectangle is not shown and offset is not zero --> show it
            connectDots(position, position + 1);
        } else if (mPagerState != PagerState.STILL && offset == 0F) {
            // glue rectangle is shown and offset is now zero --> hide it
            disconnectDots();
        }

        if (offset == 0F) {
            mCurrentItem = position;
        }

        boolean scrollingLeft = mLastScrollValue > (position + offset);
        mLastScrollValue = position + offset;

        // When user scrolls very quickly it is not possible perform animations in that short period of time
        // Instead rectangle gluing two dots is moved immediately
        // This block is executed when page has changed during scroll but offset with "0F" was not called
        if (scrollingLeft && mGluedDotLeft != position && mPagerState == PagerState.SCROLLING && offset != 0F) {
            mGluedDotRight--;
            mGluedDotLeft--;
            mCurrentItem--;
            invalidate();
        } else if (!scrollingLeft && mGluedDotRight != position + 1 && mPagerState == PagerState.SCROLLING && offset != 0F) {
            mGluedDotLeft++;
            mGluedDotRight++;
            mCurrentItem++;
            invalidate();
        }
    }

    @Override
    public void onPageSelected(int position) {
        // ignored
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // ignored
    }

    @Override
    public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
        if (newAdapter != null) {
            // New adapter might have different count of fragments...
            mDotsCount = newAdapter.getCount();
            mCurrentItem = viewPager.getCurrentItem();
            invalidate();
        } else {
            // Adapter was removed, so no dots will be drawn
            mDotsCount = 0;
            mCurrentItem = 0;
            invalidate();
        }
    }


    /**
     * Starts animation that will disconnect two already connected dots. They must not be connected before calling this method
     * however it is highly recommended to call it only if they were connected before.
     *
     */
    private void disconnectDots() {
        mPagerState = PagerState.SCROLLING;

        int difference = (int) Math.max(Math.abs(mGluedDotLeft - mGluedDotRight), mGlueRatio);
        ValueAnimator animator = ObjectAnimator.ofFloat(this, "glueRatio", difference, 0F);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPagerState = PagerState.STILL;
            }
        });
        animator.start();
    }

    /**
     * Starts animation that will connect two dots passed in parameters.
     *
     * @param position          position of any dot
     * @param siblingPosition   position of any dot
     */
    private void connectDots(final int position, final int siblingPosition) {
        mPagerState = PagerState.SCROLLING;
        mGluedDotLeft = Math.min(position, siblingPosition);
        mGluedDotRight = Math.max(position, siblingPosition);

        int difference = Math.abs(position - siblingPosition);
        ValueAnimator animator = ObjectAnimator.ofFloat(this, "glueRatio", mGlueRatio, difference);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * Connects two dots, selects target dot and eventually disconnects them.
     * Smooth and blazing fast (:
     *
     * @param currentPosition currently selected item
     * @param targetPosition  position of item that will be selected after animation will end
     */
    public void animate(final int currentPosition, final int targetPosition) {
        mPagerState = PagerState.SCROLLING;
        mGluedDotLeft = Math.min(currentPosition, targetPosition);
        mGluedDotRight = Math.max(currentPosition, targetPosition);

        int difference = Math.abs(currentPosition - targetPosition);
        final int animationDuration = (int) Math.sqrt(difference) * ANIMATION_DURATION; // simulating exponential interpolation

        ValueAnimator animator = ObjectAnimator.ofFloat(this, "glueRatio", 0F, difference);
        animator.setDuration(animationDuration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPagerState = PagerState.STILL;
                mCurrentItem = targetPosition;
                disconnectDots();
            }
        });
        animator.start();
    }

    private void setGlueRatio(float glueRatio) {
        mGlueRatio = glueRatio;
        invalidate();
    }

    /**
     * Measures width of view
     *
     * @param measureSpec specification
     * @return width depending on specification
     */
    @Px
    private int measureWidth(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.AT_MOST:
                return Math.min(mTotalWidth, size);
            case MeasureSpec.EXACTLY:
                return size;
            default:
            case MeasureSpec.UNSPECIFIED:
                return mTotalWidth;
        }
    }

    /**
     * Measures height of view
     *
     * @param measureSpec specification
     * @return height depending on specification
     */
    @Px
    private int measureHeight(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.AT_MOST:
                return Math.min(2 * mDotRadius, size);
            case MeasureSpec.EXACTLY:
                return size;
            default:
            case MeasureSpec.UNSPECIFIED:
                return 2 * mDotRadius;
        }
    }

    /**
     * Calculates position of any dot
     *
     * @param position position in range from 0 (including) to {@link #mDotsCount} (excluding)
     * @return x coordinate dot's center
     */
    private int getDotOffset(int position) {
        return position * 2 * mDotRadius + position * mSpaceBetweenDots;
    }

    /**
     * Connects this indicator with given ViewPager.
     * It will automatically listen for changes and animate.
     * Provided ViewPager may be null. All listeners from previous one will be detached
     *
     * @param pager view pager which has adapter attached.
     */
    public void setupWithViewPager(@Nullable ViewPager pager) {
        if (pager == null) {
            // Null pager, listeners must be removed
            if (mViewPager != null) {
                mViewPager.removeOnPageChangeListener(this);
                mViewPager.removeOnAdapterChangeListener(this);
            }
        } else {
            // If we had ViewPager previously then detach listener and continue
            if (mViewPager != null) {
                mViewPager.removeOnPageChangeListener(this);
                mViewPager.removeOnAdapterChangeListener(this);
            }

            mViewPager = pager;
            mViewPager.addOnPageChangeListener(this);
            mViewPager.addOnAdapterChangeListener(this);

            if (mViewPager.getAdapter() != null) {
                mDotsCount = mViewPager.getAdapter().getCount();
                mCurrentItem = pager.getCurrentItem();
            } else { // no adapter attached => no dots ):
                mDotsCount = 0;
                mCurrentItem = 0;
            }

            calculateTotalWidth();
            invalidate();

        }
    }

    /**
     * Notifies that adapter's data has changed, call this method when for example
     * total count of items in adapter changes.
     * <p>
     * Calling this method is not required when the adapter has been replaced with another one.
     */
    public void notifyDataSetChanged() {
        if (mViewPager != null) {
            if (mViewPager.getAdapter() != null) {
                mDotsCount = mViewPager.getAdapter().getCount();
                mCurrentItem = mViewPager.getCurrentItem();
            } else {
                mDotsCount = 0;
                mCurrentItem = 0;
            }
        }

        calculateTotalWidth();
        invalidate();
    }

    /**
     * Sets position of currently selected dot
     *
     * @param newPosition position of selected dot
     * @param animate     indicates if animation should be performed
     */
    public void setCurrentItem(int newPosition, boolean animate) {
        if (mViewPager != null) {
            throw new IllegalStateException("ViewPager is attached to PagerIndicator, detach it by calling #setupWithViewPager(ViewPager) and pass 'null' before calling this method");
        } else if (newPosition < 0 || newPosition >= mDotsCount) {
            throw new IllegalArgumentException("New position must be in range from 0 to " + (mDotsCount - 1) + ", provided argument is " + newPosition);
        }

        if (animate) {
            animate(mCurrentItem, newPosition);
        } else {
            mCurrentItem = newPosition;
            invalidate();
        }
    }

    /**
     * @return currently selected dot. This number is in rage from 0 to {@link #mDotRadius} - 1
     */
    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * Sets position of currently selected dot, no animation is performed by default
     *
     * @param newPosition position of selected dot
     */
    public void setCurrentItem(int newPosition) {
        setCurrentItem(newPosition, false);
    }

    /**
     * @return color of active dot
     */
    @ColorInt
    public int getActiveColor() {
        return mPaintActive.getColor();
    }

    /**
     * Sets color of active dot
     *
     * @param activeColor color of active dot
     */
    public void setActiveColor(@ColorInt int activeColor) {
        mPaintActive.setColor(activeColor);
        invalidate();
    }

    /**
     * @return color of inactive dots
     */
    @ColorInt
    public int getInactiveColor() {
        return mPaintInactive.getColor();
    }

    /**
     * Sets color of inactive dots
     *
     * @param inactiveColor color of inactive dots
     */
    public void setInactiveColor(@ColorInt int inactiveColor) {
        mPaintActive.setColor(inactiveColor);
        invalidate();
    }

    /**
     * @param spaceBetweenDots total space in px between dots
     */
    public void setSpaceBetweenDots(@Px int spaceBetweenDots) {
        mSpaceBetweenDots = spaceBetweenDots;
        calculateTotalWidth();
        invalidate();
    }

    /**
     * @param dotRadius radius of one dot
     */
    public void setDotRadius(@Px int dotRadius) {
        mDotRadius = dotRadius;
    }

    /**
     * @return total space between two neighbouring dots
     */
    @Px
    public int getSpaceBetweenDots() {
        return mSpaceBetweenDots;
    }

    /**
     * @return count of displayed dots
     */
    public int getDotsCount() {
        return mDotsCount;
    }

    /**
     * Sets total count of drawn dots. Call this method only when no ViewPager is attached or
     *
     * @param dotsCount total count of drawn dots
     */
    public void setDotsCount(int dotsCount) {
        if (mViewPager != null) {
            throw new IllegalStateException("ViewPager is attached to PagerIndicator, detach it by calling #setupWithViewPager(ViewPager) and pass 'null' before calling this method");
        }

        mDotsCount = dotsCount;
        calculateTotalWidth();
        invalidate();
    }

    /**
     * @return radius of one dot in pixels
     */
    @Px
    public int getDotRadius() {
        return mDotRadius;
    }
}
