package com.yl.slidebottomdrawer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.Scroller
import kotlin.math.abs


/**
 * author:lyliuhuo
 * desc:底部上下滑动抽屉控件
 */
class SlideBottomDrawer(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {
    var childStop: Boolean = true

    //当前状态 -1中间 0底部 1顶部
    private var mCurrentFlag: Int = 0

    //下次滑动的初始状态
    private var startOnTop = false
    private var startOnBottom = true

    //手指按下 抬起纵轴位置
    private var mDownY = 0f
    private var mUpY = 0f

    //手指移动距离
    private var moveDis = 0f

    //按下手指
    private var startY = 0f

    //滑动超过系数应该收起或者展开了
    private val hideWeight = 0.2f

    //收起时的高度
    private var visibleHeight = 0f
    private var mTouchSlop = 0
    private lateinit var mScroller: Scroller

    //唯一子View
    private var mChildView: View? = null

    //可以移动的距离 即 展开高度与收起高度的差
    private var moveMaxDis = 0

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    init {
        initAttrs(context, attributeSet)
    }

    private fun initAttrs(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SlideBottomDrawer)
        visibleHeight = typedArray.getDimension(R.styleable.SlideBottomDrawer_visible_height, 0f)
        typedArray.recycle()
        //点击与滑动阈值
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        initConfig()
    }

    private fun initConfig() {
        mScroller = Scroller(context)
        setBackgroundColor(Color.TRANSPARENT)
    }

    //展开
    private fun show() {
        mScroller.startScroll(0, scrollY, 0, (moveMaxDis - scrollY))
        postInvalidate()
        mCurrentFlag = 1
        startOnBottom = false
        startOnTop = true
    }

    //收起
    private fun hide() {
        mScroller.startScroll(0, scrollY, 0, -scrollY)
        postInvalidate()
        mCurrentFlag = 0
        startOnBottom = true
        startOnTop = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount == 0 || getChildAt(0) == null) {
            throw RuntimeException("There have no ChildView in the SlideBottomDrawer!")
        }
        if (childCount > 1) {
            throw RuntimeException("There just allowed one ChildView in the SlideBottomDrawer!")
        }
        mChildView = getChildAt(0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        moveMaxDis = (mChildView!!.measuredHeight - visibleHeight).toInt()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mChildView!!.layout(
            0,
            moveMaxDis,
            mChildView!!.measuredWidth,
            mChildView!!.measuredHeight + moveMaxDis
        )
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.currY)
            postInvalidate()
        }
    }

    //控制点击和滑动的分发
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val dy = ev!!.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownY = dy
                startY = dy
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                if (abs(dy - startY) >= mTouchSlop) {
                    if (childScroll(dy - startY)) {
                        return super.onInterceptTouchEvent(ev)
                    }
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    //若内部是可滑动的RecyclerView 需要通过这个来判断recyclerView是否在滑动
    private fun childScroll(fl: Float): Boolean {
        //只有在顶部向下才需要此判断
        if (mCurrentFlag == 1) {
            return !childStop || fl <= 0
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val dy = event!!.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownY = dy
                return mCurrentFlag == 1 || mDownY >= moveMaxDis
            }
            MotionEvent.ACTION_MOVE -> {
                mUpY = dy
                val moveY = mDownY - mUpY
                moveDis += moveY
                if ((mCurrentFlag == 1 && moveDis > 0) || (mCurrentFlag == 0 && moveDis <= 0)) {
                    mDownY = mUpY
                    return super.onTouchEvent(event)
                }
                if ((startOnBottom && moveDis < 0) || (startOnTop && moveDis > 0)) {
                    mDownY = mUpY
                    return super.onTouchEvent(event)
                }
                //超出最大可滑动距离
                if (abs(moveDis) >= moveMaxDis) {
                    if (moveDis > 0) {
                        mCurrentFlag = 1
                        startOnTop = true
                        startOnBottom = false
                    } else {
                        mCurrentFlag = 0
                        startOnBottom = true
                        startOnTop = false
                    }
                    mDownY = mUpY
                } else {
                    mCurrentFlag = -1
                    scrollBy(0, moveY.toInt())
                    mDownY = mUpY
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (startOnBottom && moveDis < 0) {
                    moveDis = 0f
                    hide()
                    return super.onTouchEvent(event)
                } else if (startOnTop && moveDis > 0) {
                    moveDis = 0f
                    show()
                    return super.onTouchEvent(event)
                }
                if (abs(moveDis) >= moveMaxDis) {
                    if (moveDis > 0) {
                        mCurrentFlag = 1
                        startOnBottom = false
                        startOnTop = true
                    } else {
                        mCurrentFlag = 0
                        startOnBottom = true
                        startOnTop = false
                    }
                } else {
                    if (abs(moveDis) >= moveMaxDis * hideWeight) {
                        if (moveDis > 0) {
                            show()
                        } else {
                            hide()
                        }
                    } else {
                        if (mCurrentFlag == -1) {
                            if (moveDis > 0) {
                                hide()
                            } else {
                                show()
                            }
                        }
                    }
                }
                moveDis = 0f
            }
        }
        return super.onTouchEvent(event)
    }
}