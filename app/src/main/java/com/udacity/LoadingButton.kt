package com.udacity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var btnText = ""
    private var currentBtnAnimationValue = 0f
    private var currentProgressAnimationValue = 0f

    private var buttonDefaultBackgroundColor = 0
    private var buttonBackgroundColor = 0
    private var buttonDefaultText: CharSequence = ""
    private var buttonText: CharSequence = ""
    private var buttonTextColor = 0
    private var progressCircleBackgroundColor = 0

    private var progressValueAnimator = ValueAnimator()
    private var btnValueAnimator = ValueAnimator()
    private lateinit var btnTxtBounds: Rect
    private val progressCRect = RectF()
    private var progressCSize = 0f

    private val animatorSetBtn: AnimatorSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(3)
        doOnStart { isEnabled = false }
        doOnEnd { isEnabled = true }
    }

    private val btnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, newState ->
        when (newState) {
            ButtonState.Loading -> {
                btnText = buttonText.toString()

                animatorSetBtn.start()
                if (!::btnTxtBounds.isInitialized) {
                    btnTxtBounds = Rect()
                    btnTextPaint.getTextBounds(btnText, 0, btnText.length, btnTxtBounds)
                    val hCenter = (btnTxtBounds.right + btnTxtBounds.width() + 16f)
                    val vCenter = (heightSize / 2f)

                    progressCRect.set(
                        hCenter - progressCSize,
                        vCenter - progressCSize,
                        hCenter + progressCSize,
                        vCenter + progressCSize
                    )
                }
                invalidate()
                requestLayout()
            }
            else -> {
                btnText = buttonDefaultText.toString()
                animatorSetBtn.cancel()
                invalidate()
                requestLayout()
            }
        }
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonDefaultBackgroundColor = getColor(R.styleable.LoadingButton_loadingDefaultBackgroundColor, 0)
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            buttonDefaultText = getText(R.styleable.LoadingButton_loadingDefaultText)
            buttonText = getText(R.styleable.LoadingButton_loadingText)
            buttonTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, 0)
        }.also {
            btnText = buttonDefaultText.toString()
            progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
        }

        animateFunctions()
    }

    private fun animateFunctions() {
        progressValueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            addUpdateListener {
                currentProgressAnimationValue = this.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawButtonBackground()
            drawButtonText()
        }
    }

    private fun Canvas.drawButtonBackground() {
        when (buttonState) {
            ButtonState.Loading -> {
                btnPaint.apply {
                    color = buttonBackgroundColor
                    drawRect(0f, 0f, currentBtnAnimationValue, heightSize.toFloat(), this)
                    color = buttonDefaultBackgroundColor
                    drawRect(currentBtnAnimationValue, 0f, widthSize.toFloat(), heightSize.toFloat(), this)
                }
                btnPaint.color = progressCircleBackgroundColor
                drawArc(progressCRect, 0f, currentProgressAnimationValue, true, btnPaint)
            }
            else -> {
                drawColor(buttonDefaultBackgroundColor)
            }
        }
    }

    private fun Canvas.drawButtonText() {
        btnTextPaint.color = buttonTextColor
        drawText(btnText, (widthSize / 2f), (heightSize / 2f) + btnTextPaint.computeTxtOffset(), btnTextPaint)
    }

    private fun TextPaint.computeTxtOffset() = ((descent() - ascent()) / 2) - descent()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        btnValueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                currentBtnAnimationValue = it.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }

        animatorSetBtn.playTogether(progressValueAnimator, btnValueAnimator)
        progressCSize = (min(w, h) / 2f) * 0.4f
    }

    fun changeButtonState(state: ButtonState) {
        buttonState = state
    }

    override fun performClick(): Boolean {
        super.performClick()
        // We only change button state to Clicked if the current state is Completed
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }
}
