package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs


class MyDrawView(c: Context?) : View(c) {
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mPath: Path = Path()
    private var mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
    private val mPaint: Paint = Paint()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        if (mBitmap != null) {
            mCanvas = Canvas(mBitmap!!)
        }
    }

    override fun getDrawingCache(): Bitmap {
        return super.getDrawingCache()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(mBitmap!!, 0F, 0F, mBitmapPaint)
        canvas.drawPath(mPath, mPaint)
    }

    private var mX = 0f
    private var mY = 0f

    init {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = -0x1000000
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 3F
    }

    private fun touchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
        // commit the path to our offscreen
        mCanvas?.drawPath(mPath, mPaint)
        // kill this so we don't double draw
        mPath.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    fun clear() {
        mBitmap!!.eraseColor(Color.TRANSPARENT)
        invalidate()
        System.gc()
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}