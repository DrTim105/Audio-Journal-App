package com.salihutimothy.myaudiojournalapp.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.salihutimothy.myaudiojournalapp.R

class WaveformView(context: Context?, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var spikeWidth = 6f
    private var distance = 6f
    private var screenWidth = 0f
    private var screenHeight = 500f

    private var maxSpikes = 0

    init {
//        paint.color = Color.rgb(244, 81, 13)

        paint.color = ContextCompat.getColor(context!!, (R.color.accentz))

        screenWidth = resources.displayMetrics.widthPixels.toFloat()


        maxSpikes = (screenWidth / (spikeWidth + distance)).toInt()
    }

    fun addAmplitude(amp: Float) {
        val norm = Math.min((amp.toInt()/50), 400).toFloat()
        amplitudes.add(norm)

        spikes.clear()
        val amps = amplitudes.takeLast(maxSpikes - 10)

        for (i: Int in amps.indices) {
            val left = i * (spikeWidth + distance)
            val top = screenHeight/2 - amps[i]/2
            val right = left + spikeWidth
            val bottom = top + amps[i]

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