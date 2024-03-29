package com.belcobtm.presentation.core.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.belcobtm.R

class SwipeRevealLayout : ViewGroup {
    /**
     * Main view is the view which is shown when the layout is closed.
     */
    private var mMainView: View? = null

    /**
     * Secondary view is the view which is shown when the layout is opened.
     */
    private var mSecondaryView: View? = null

    /**
     * The rectangle position of the main view when the layout is closed.
     */
    private val mRectMainClose = Rect()

    /**
     * The rectangle position of the main view when the layout is opened.
     */
    private val mRectMainOpen = Rect()

    /**
     * The rectangle position of the secondary view when the layout is closed.
     */
    private val mRectSecClose = Rect()

    /**
     * The rectangle position of the secondary view when the layout is opened.
     */
    private val mRectSecOpen = Rect()

    /**
     * The minimum distance (px) to the closest drag edge that the SwipeRevealLayout
     * will disallow the parent to intercept touch event.
     */
    private var mMinDistRequestDisallowParent = 0
    private var mIsOpenBeforeInit = false

    @Volatile
    private var mIsScrolling = false

    /**
     * @return true if the drag/swipe motion is currently locked.
     */
    @Volatile
    var isDragLocked = false
        private set
    private var mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY
    private var mMode = MODE_NORMAL
    private var mDragEdge = DRAG_EDGE_LEFT
    private var mDragDist = 0f
    private var mPrevX = -1f
    private lateinit var mDragHelper: ViewDragHelper
    private lateinit var mGestureDetector: GestureDetectorCompat

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Nullable
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(SUPER_INSTANCE_STATE, super.onSaveInstanceState())
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle?
        bundle ?: return
        super.onRestoreInstanceState(bundle.getParcelable(SUPER_INSTANCE_STATE))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(event)
        mDragHelper.processTouchEvent(event)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isDragLocked) {
            return super.onInterceptTouchEvent(ev)
        }
        mDragHelper.processTouchEvent(ev)
        mGestureDetector.onTouchEvent(ev)
        accumulateDragDist(ev)
        val couldBecomeClick = couldBecomeClick(ev)
        val settling = mDragHelper.viewDragState == ViewDragHelper.STATE_SETTLING
        val idleAfterScrolled = (mDragHelper.viewDragState == ViewDragHelper.STATE_IDLE
                && mIsScrolling)

        // must be placed as the last statement
        mPrevX = ev.x

        // return true => intercept, cannot trigger onClick event
        return !couldBecomeClick && (settling || idleAfterScrolled)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // get views
        if (childCount >= 2) {
            mSecondaryView = getChildAt(0)
            mMainView = getChildAt(1)
        } else if (childCount == 1) {
            mMainView = getChildAt(0)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            var left: Int
            var right: Int
            var top: Int
            var bottom: Int
            bottom = 0
            top = bottom
            right = top
            left = right
            val minLeft = paddingLeft
            val maxRight = Math.max(r - paddingRight - l, 0)
            val minTop = paddingTop
            val maxBottom = Math.max(b - paddingBottom - t, 0)
            var measuredChildHeight = child.measuredHeight
            var measuredChildWidth = child.measuredWidth

            // need to take account if child size is match_parent
            val childParams = child.layoutParams
            var matchParentHeight = false
            var matchParentWidth = false
            if (childParams != null) {
                matchParentHeight = childParams.height == LayoutParams.MATCH_PARENT
                matchParentWidth = childParams.width == LayoutParams.MATCH_PARENT
            }
            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop
                childParams!!.height = measuredChildHeight
            }
            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft
                childParams!!.width = measuredChildWidth
            }
            when (mDragEdge) {
                DRAG_EDGE_RIGHT -> {
                    left = Math.max(r - measuredChildWidth - paddingRight - l, minLeft)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.max(r - paddingRight - l, minLeft)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }
                DRAG_EDGE_LEFT -> {
                    left = Math.min(paddingLeft, maxRight)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.min(measuredChildWidth + paddingLeft, maxRight)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }
            }
            child.layout(left, top, right, bottom)
        }

        // taking account offset when mode is SAME_LEVEL
        if (mMode == MODE_SAME_LEVEL) {
            when (mDragEdge) {
                DRAG_EDGE_LEFT -> mSecondaryView!!.offsetLeftAndRight(-mSecondaryView!!.width)
                DRAG_EDGE_RIGHT -> mSecondaryView!!.offsetLeftAndRight(
                    mSecondaryView!!.width
                )
            }
        }
        initRects()
        if (mIsOpenBeforeInit) {
            open(false)
        } else {
            close(false)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthMeasureSpec = widthSpec
        var heightMeasureSpec = heightSpec
        if (childCount < 2) {
            throw RuntimeException("Layout must have two children")
        }
        val params = layoutParams
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var desiredWidth = 0
        var desiredHeight = 0

        // first find the largest child
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }
        // create new measure spec using the largest child width
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, widthMode)
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, heightMode)
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childParams = child.layoutParams
            if (childParams != null) {
                if (childParams.height == LayoutParams.MATCH_PARENT) {
                    child.minimumHeight = measuredHeight
                }
                if (childParams.width == LayoutParams.MATCH_PARENT) {
                    child.minimumWidth = measuredWidth
                }
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }

        // taking accounts of padding
        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        // adjust desired width
        if (widthMode == MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth
        } else {
            if (params.width == LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                desiredWidth = if (desiredWidth > measuredWidth) measuredWidth else desiredWidth
            }
        }

        // adjust desired height
        if (heightMode == MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight
        } else {
            if (params.height == LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight
            }
            if (heightMode == MeasureSpec.AT_MOST) {
                desiredHeight =
                    if (desiredHeight > measuredHeight) measuredHeight else desiredHeight
            }
        }
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * Open the panel to show the secondary view
     */
    fun open(animation: Boolean) {
        val view = mMainView ?: return
        mIsOpenBeforeInit = true
        if (animation) {
            mDragHelper.smoothSlideViewTo(view, mRectMainOpen.left, mRectMainOpen.top)
        } else {
            mDragHelper.abort()
            mMainView?.layout(
                mRectMainOpen.left,
                mRectMainOpen.top,
                mRectMainOpen.right,
                mRectMainOpen.bottom
            )
            mSecondaryView!!.layout(
                mRectSecOpen.left,
                mRectSecOpen.top,
                mRectSecOpen.right,
                mRectSecOpen.bottom
            )
        }
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Close the panel to hide the secondary view
     */
    fun close(animation: Boolean) {
        val view = mMainView ?: return
        mIsOpenBeforeInit = false
        if (animation) {
            mDragHelper.smoothSlideViewTo(view, mRectMainClose.left, mRectMainClose.top)
        } else {
            mDragHelper.abort()
            view.layout(
                mRectMainClose.left,
                mRectMainClose.top,
                mRectMainClose.right,
                mRectMainClose.bottom
            )
            mSecondaryView!!.layout(
                mRectSecClose.left,
                mRectSecClose.top,
                mRectSecClose.right,
                mRectSecClose.bottom
            )
        }
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * @return Set true for lock the swipe.
     */
    fun dragLock(drag: Boolean) {
        isDragLocked = drag
    }

    private val mainOpenLeft: Int
        get() = when (mDragEdge) {
            DRAG_EDGE_LEFT -> mRectMainClose.left + mSecondaryView!!.width
            DRAG_EDGE_RIGHT -> mRectMainClose.left - mSecondaryView!!.width
            else -> 0
        }
    private val mainOpenTop: Int
        get() {
            return when (mDragEdge) {
                DRAG_EDGE_LEFT -> mRectMainClose.top
                DRAG_EDGE_RIGHT -> mRectMainClose.top
                else -> 0
            }
        }
    private val secOpenLeft: Int
        get() = mRectSecClose.left
    private val secOpenTop: Int
        get() = mRectSecClose.top

    private fun initRects() {
        // close position of main view
        mRectMainClose[mMainView!!.left, mMainView!!.top, mMainView!!.right] = mMainView!!.bottom

        // close position of secondary view
        mRectSecClose[mSecondaryView!!.left, mSecondaryView!!.top, mSecondaryView!!.right] =
            mSecondaryView!!.bottom

        // open position of the main view
        mRectMainOpen[mainOpenLeft, mainOpenTop, mainOpenLeft + mMainView!!.width] =
            mainOpenTop + mMainView!!.height

        // open position of the secondary view
        mRectSecOpen[secOpenLeft, secOpenTop, secOpenLeft + mSecondaryView!!.width] =
            secOpenTop + mSecondaryView!!.height
    }

    private fun couldBecomeClick(ev: MotionEvent): Boolean {
        return isInMainView(ev) && !shouldInitiateADrag()
    }

    private fun isInMainView(ev: MotionEvent): Boolean {
        val x = ev.x
        val y = ev.y
        val withinVertical = mMainView!!.top <= y && y <= mMainView!!.bottom
        val withinHorizontal = mMainView!!.left <= x && x <= mMainView!!.right
        return withinVertical && withinHorizontal
    }

    private fun shouldInitiateADrag(): Boolean {
        val minDistToInitiateDrag: Float = mDragHelper.touchSlop.toFloat()
        return mDragDist >= minDistToInitiateDrag
    }

    private fun accumulateDragDist(ev: MotionEvent) {
        val action = ev.action
        if (action == MotionEvent.ACTION_DOWN) {
            mDragDist = 0f
            return
        }
        val dragged = Math.abs(ev.x - mPrevX)
        mDragDist += dragged
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        if (attrs != null && context != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SwipeRevealLayout,
                0, 0
            )
            mDragEdge = a.getInteger(R.styleable.SwipeRevealLayout_dragFromEdge, DRAG_EDGE_LEFT)
            mMode = MODE_NORMAL
            mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY
            mMinDistRequestDisallowParent = DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT
        }
        mDragHelper = ViewDragHelper.create(this, 1.0f, mDragHelperCallback)
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
        context?.let { mGestureDetector = GestureDetectorCompat(it, mGestureListener) }
    }

    private val mGestureListener: GestureDetector.OnGestureListener =
        object : SimpleOnGestureListener() {
            var hasDisallowed = false
            override fun onDown(e: MotionEvent): Boolean {
                mIsScrolling = false
                hasDisallowed = false
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                mIsScrolling = true
                return false
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                mIsScrolling = true
                if (parent != null) {
                    val shouldDisallow: Boolean
                    if (!hasDisallowed) {
                        shouldDisallow = distToClosestEdge >= mMinDistRequestDisallowParent
                        if (shouldDisallow) {
                            hasDisallowed = true
                        }
                    } else {
                        shouldDisallow = true
                    }

                    // disallow parent to intercept touch event so that the layout will work
                    // properly on RecyclerView or view that handles scroll gesture.
                    parent.requestDisallowInterceptTouchEvent(shouldDisallow)
                }
                return false
            }
        }
    private val distToClosestEdge: Int
        get() {
            when (mDragEdge) {
                DRAG_EDGE_LEFT -> {
                    val pivotRight = mRectMainClose.left + mSecondaryView!!.width
                    return Math.min(
                        mMainView!!.left - mRectMainClose.left,
                        pivotRight - mMainView!!.left
                    )
                }
                DRAG_EDGE_RIGHT -> {
                    val pivotLeft = mRectMainClose.right - mSecondaryView!!.width
                    return Math.min(
                        mMainView!!.right - pivotLeft,
                        mRectMainClose.right - mMainView!!.right
                    )
                }
            }
            return 0
        }
    private val halfwayPivotHorizontal: Int
        get() {
            return if (mDragEdge == DRAG_EDGE_LEFT) {
                mRectMainClose.left + mSecondaryView!!.width / 2
            } else {
                mRectMainClose.right - mSecondaryView!!.width / 2
            }
        }
    private val mDragHelperCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (isDragLocked) return false
            mMainView?.let { mDragHelper.captureChildView(it, pointerId) }
            return false
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return when (mDragEdge) {
                DRAG_EDGE_RIGHT -> Math.max(
                    Math.min(left, mRectMainClose.left),
                    mRectMainClose.left - mSecondaryView!!.width
                )
                DRAG_EDGE_LEFT -> Math.max(
                    Math.min(left, mRectMainClose.left + mSecondaryView!!.width),
                    mRectMainClose.left
                )
                else -> child.left
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val velRightExceeded = pxToDp(xvel.toInt()) >= mMinFlingVelocity
            val velLeftExceeded = pxToDp(xvel.toInt()) <= -mMinFlingVelocity
            val pivotHorizontal = halfwayPivotHorizontal
            when (mDragEdge) {
                DRAG_EDGE_RIGHT -> if (velRightExceeded) {
                    close(true)
                } else if (velLeftExceeded) {
                    open(true)
                } else {
                    if (mMainView!!.right < pivotHorizontal) {
                        open(true)
                    } else {
                        close(true)
                    }
                }
                DRAG_EDGE_LEFT -> if (velRightExceeded) {
                    open(true)
                } else if (velLeftExceeded) {
                    close(true)
                } else {
                    if (mMainView!!.left < pivotHorizontal) {
                        close(true)
                    } else {
                        open(true)
                    }
                }
            }
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            super.onEdgeDragStarted(edgeFlags, pointerId)
            if (isDragLocked) {
                return
            }
            val edgeStartLeft = (mDragEdge == DRAG_EDGE_RIGHT
                    && edgeFlags == ViewDragHelper.EDGE_LEFT)
            val edgeStartRight = (mDragEdge == DRAG_EDGE_LEFT
                    && edgeFlags == ViewDragHelper.EDGE_RIGHT)
            if (edgeStartLeft || edgeStartRight) {
                mMainView?.let { mDragHelper.captureChildView(it, pointerId) }
            }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            if (mMode == MODE_SAME_LEVEL) {
                if (mDragEdge == DRAG_EDGE_LEFT || mDragEdge == DRAG_EDGE_RIGHT) {
                    mSecondaryView!!.offsetLeftAndRight(dx)
                } else {
                    mSecondaryView!!.offsetTopAndBottom(dy)
                }
            }
            ViewCompat.postInvalidateOnAnimation(this@SwipeRevealLayout)
        }
    }

    private fun pxToDp(px: Int): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return (px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    companion object {
        private const val SUPER_INSTANCE_STATE = "saved_instance_state_parcelable"
        private const val DEFAULT_MIN_FLING_VELOCITY = 300 // dp per second
        private const val DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT = 1 // dp
        const val DRAG_EDGE_LEFT = 0x1
        const val DRAG_EDGE_RIGHT = 0x1 shl 1

        /**
         * The secondary view will be under the main view.
         */
        const val MODE_NORMAL = 0

        /**
         * The secondary view will stick the edge of the main view.
         */
        const val MODE_SAME_LEVEL = 1
    }
}