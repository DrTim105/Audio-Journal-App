package com.salihutimothy.myaudiojournalapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context?, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var spikeWidth = 9f
    private var distance = 6f
    private var screenWidth = 0f
    private var screenHeight = 500f

    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(244, 81, 13)

        screenWidth = resources.displayMetrics.widthPixels.toFloat()

        maxSpikes = (screenWidth / (spikeWidth + distance)).toInt()
    }

    fun addAmplitude(amp: Float) {
        var norm = Math.min((amp.toInt()/50), 400).toFloat()
        amplitudes.add(norm)

        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i: Int in amps.indices) {
            var left = screenWidth - i * (spikeWidth + distance)
            var top = screenHeight/2 - amps[i]/2
            var right = left + spikeWidth
            var bottom = top + amps[i]

            spikes.add(RectF(left, top, right, bottom))

        }

        invalidate()
    }

    fun clear(){
        amplitudes.clear()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        spikes.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }


}