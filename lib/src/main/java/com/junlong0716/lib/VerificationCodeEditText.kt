package com.junlong0716.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import android.opengl.ETC1.getWidth
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowManager



class VerificationCodeEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs), View.OnFocusChangeListener{
    //文字颜色
    private var mTextColor: Int = 0
    // 输入的最大长度
    private var mMaxLength = 4
    // 边框宽度
    private var mStrokeBoxWidth: Int = 0
    // 边框高度
    private var mStrokeBoxHeight: Int = 0
    // 边框之间的距离
    private var mStrokePadding = 20
    //输入结束监听
    private var mOnInputFinishListener: OnTextFinishListener? = null
    //画矩形
    private val mRect = Rect()
    //shape normal
    private var mGradientNormalDrawable: GradientDrawable? = null
    //shape select
    private var mGradientSelectDrawable: GradientDrawable? = null
    //设置圆角
    private var mRadius = 0f
    //边框宽度
    private var mStrokeWidth = 0
    //未选中边框颜色
    private var mStrokeNormalColor = Color.parseColor("#ffffff")
    //选中边框颜色
    private var mStrokeSelectColor = Color.parseColor("#ffffff")
    //选中未选中背景颜色
    private val mBgSelectColors = IntArray(2)
    //未选中背景颜色
    private val mBgNormalColors = IntArray(2)
    private val mContext = context

    interface OnTextFinishListener {
        fun onTextFinish(text: CharSequence, length: Int)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeEditText)
        val indexCount = typedArray.indexCount
        for (i in 0 until indexCount) {
            when (val index = typedArray.getIndex(i)) {
                R.styleable.VerificationCodeEditText_strokeBoxHeight -> this.mStrokeBoxHeight = typedArray.getDimension(index, 60f).toInt()
                R.styleable.VerificationCodeEditText_strokeBoxWidth -> this.mStrokeBoxWidth = typedArray.getDimension(index, 60f).toInt()
                R.styleable.VerificationCodeEditText_strokePadding -> this.mStrokePadding = typedArray.getDimension(index, 20f).toInt()
                R.styleable.VerificationCodeEditText_strokeLength -> this.mMaxLength = typedArray.getInteger(index, 4)
                R.styleable.VerificationCodeEditText_strokeRadius -> this.mRadius = typedArray.getDimension(index, 0f)
                R.styleable.VerificationCodeEditText_strokeWidth -> this.mStrokeWidth = typedArray.getDimension(index, 0f).toInt()
                R.styleable.VerificationCodeEditText_strokeNormalColor -> this.mStrokeNormalColor = typedArray.getColor(index, Color.parseColor("#000000"))
                R.styleable.VerificationCodeEditText_strokeSelectColor -> this.mStrokeSelectColor = typedArray.getColor(index, Color.parseColor("#000000"))
                R.styleable.VerificationCodeEditText_strokeBoxNormalBgColor -> {
                    mBgNormalColors[0] = typedArray.getColor(index, Color.parseColor("#ffffff"))
                    mBgNormalColors[1] = typedArray.getColor(index, Color.parseColor("#ffffff"))
                }
                R.styleable.VerificationCodeEditText_strokeBoxSelectBgColor -> {
                    mBgSelectColors[0] = typedArray.getColor(index, Color.parseColor("#ffffff"))
                    mBgSelectColors[1] = typedArray.getColor(index, Color.parseColor("#ffffff"))
                }
            }
        }

        typedArray.recycle()

        //未选中
        mGradientNormalDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBgNormalColors)
        with(mGradientNormalDrawable!!) {
            setStroke(mStrokeWidth, mStrokeNormalColor!!)
            cornerRadius = mRadius
        }
        //选中
        mGradientSelectDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBgSelectColors)
        with(mGradientSelectDrawable!!) {
            setStroke(mStrokeWidth, mStrokeSelectColor!!)
            cornerRadius = mRadius
        }

        setMaxLength(mMaxLength)
        // 去掉背景颜色
        setBackgroundColor(Color.TRANSPARENT)
        // 不显示光标
        isCursorVisible = false
        // 取消长按事件
        isLongClickable = false
        //获取焦点监听
        onFocusChangeListener = this
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus)
            (v as AppCompatEditText).setSelection(this.text!!.length)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return false
    }

    //设置最大长度
    private fun setMaxLength(maxLength: Int) {
        filters = if (maxLength >= 0) {
            arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        } else {
            arrayOfNulls(0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 判断高度是否小于推荐高度
        if (height < mStrokeBoxHeight) {
            height = mStrokeBoxHeight
        }
        // 判断高度是否小于推荐宽度
        val recommendWidth = mStrokeBoxWidth * mMaxLength + mStrokePadding * (mMaxLength - 1)
        //屏幕宽度
        val screenWidth = mContext.resources.displayMetrics.widthPixels

        if(screenWidth < recommendWidth){
            //todo 解决适配问题
            Log.e("Layout Error","大小超过屏幕宽度，将显示不全！")
        }

        if (width < recommendWidth) {
            width = recommendWidth
        }

        Log.d("width",width.toString())
        Log.d("recommendWidth",recommendWidth.toString())
        Log.d("screenWidth",screenWidth.toString())
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode)
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        mTextColor = currentTextColor
        setTextColor(Color.TRANSPARENT)
        setTextColor(mTextColor)
        // 重绘背景颜色
        drawStrokeBackground(canvas)
        // 重绘文本
        drawText(canvas)
    }


    //重绘背景
    private fun drawStrokeBackground(canvas: Canvas) {
        // 绘制方框背景颜色
        mRect.left = 0
        mRect.top = 0
        mRect.right = mStrokeBoxWidth
        mRect.bottom = mStrokeBoxHeight
        val count = canvas.saveCount
        canvas.save()
        for (i in 0 until mMaxLength) {
            with(mGradientNormalDrawable!!) {
                bounds = mRect
                state = intArrayOf(android.R.attr.state_enabled)
                draw(canvas)
            }
            val dx = (mRect.right + mStrokePadding).toFloat()
            // 移动画布
            canvas.save()
            canvas.translate(dx, 0f)
        }
        canvas.restoreToCount(count)
        canvas.translate(0f, 0f)
        // 绘制激活状态的边框
        // 当前激活的索引
        val activatedIndex = Math.max(0, editableText.length)
        mRect.left = mStrokeBoxWidth * activatedIndex + mStrokePadding * activatedIndex
        mRect.right = mRect.left + mStrokeBoxWidth
        with(mGradientSelectDrawable!!) {
            state = intArrayOf(android.R.attr.state_focused)
            bounds = mRect
            draw(canvas)
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (selStart == selEnd) setSelection(text!!.length)//光标只能在最后边
    }

    //重绘文本
    private fun drawText(canvas: Canvas) {
        val count = canvas.saveCount
        canvas.translate(0f, 0f)
        val length = editableText.length
        for (i in 0 until length) {
            val text = editableText[i].toString()
            val textPaint = paint
            textPaint.color = mTextColor
            // 获取文本大小
            textPaint.getTextBounds(text, 0, 1, mRect)
            // 计算(x,y) 坐标
            val x = mStrokeBoxWidth / 2 + (mStrokeBoxWidth + mStrokePadding) * i - mRect.centerX()
            val y = canvas.height / 2 + mRect.height() / 2
            canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
        }
        canvas.restoreToCount(count)
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        // 当前文本长度
        val textLength = editableText.length
        if (textLength == mMaxLength) {
            hideSoftInput()
            if (mOnInputFinishListener != null) {
                mOnInputFinishListener!!.onTextFinish(editableText.toString(), mMaxLength)
            }
        }
    }

    //隐藏软件盘
    private fun hideSoftInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    //设置输入完成监听
    fun setOnTextFinishListener(onInputFinishListener: OnTextFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener
    }
}
