/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package seigneur.gauvain.mycourt.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet

/**
 * Widget from the great app "Plaid" of Nick Butcher : https://github.com/nickbutcher/plaid
 *
 * A extension of ForegroundImageView that is always 4:3 aspect ratio.
 */
open class ParallaxImageView(context: Context, attrs: AttributeSet) : FourThreeImageView(context, attrs) {
    private var minOffset: Float = 0f
    private var parallaxFactor = -0.5f
    private var clipB:Rect = Rect()

    fun setOffset(offset: Float) {
        val inOffset = Math.max(minOffset, offset)
        if (inOffset != translationY) {
            translationY=inOffset
            imageOffset = (inOffset * parallaxFactor).toInt()
            clipB.set(0, -inOffset.toInt(), width, height)
            clipBounds=clipB
            postInvalidateOnAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > minimumHeight) {
            minOffset = minimumHeight.toFloat() - h
        }
    }

    override fun onDraw(canvas: Canvas) {
        val paint=Paint()
        paint.color= resources.getColor(android.R.color.transparent)
        paint.alpha=0
        if (imageOffset != 0) {
            val saveCount = canvas.save()
            canvas.translate(0f, imageOffset.toFloat())
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            canvas.restoreToCount(saveCount)
        } else {
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }
    }

    companion object {
        var imageOffset: Int = 0
    }

}