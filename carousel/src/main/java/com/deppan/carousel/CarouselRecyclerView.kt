package com.deppan.carousel

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class CarouselRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    private var scaleOffset: Float = 1f
    private var scaleFactor: Float = 1f
    private var spreadFactor: Double = 100.0

    private var activeColor: Int
    private var inactiveColor: Int
    private var viewsToChangeColor: List<Int> = listOf()
    private val observer: AdapterDataObserver

    init {
        observer = object : AdapterDataObserver() {
            override fun onChanged() {
                post {
                    val slidePadding = (width / 2) - (getChildAt(0).width / 2)
                    setPadding(slidePadding, 0, slidePadding, 0)
                    scrollToPosition(0)
                    addOnScrollListener(object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            onScrollChanged()
                        }
                    })
                }
            }
        }
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        with(context.theme) {
            val typedValue = TypedValue()
            resolveAttribute(R.attr.colorControlActivated, typedValue, true)
            activeColor = typedValue.data
            resolveAttribute(R.attr.colorControlNormal, typedValue, true)
            inactiveColor = typedValue.data
        }

        val array = context.obtainStyledAttributes(attrs, R.styleable.CarouselRecyclerView, 0, 0)
        (0 until array.indexCount).forEach { styleable ->
            when (styleable) {
                R.styleable.CarouselRecyclerView_scaleOffset -> {
                    scaleOffset = array.getFloat(styleable, scaleOffset)
                }
                R.styleable.CarouselRecyclerView_scaleFactor -> {
                    scaleFactor = array.getFloat(styleable, scaleFactor)
                }
                R.styleable.CarouselRecyclerView_spreadFactor -> {
                    spreadFactor = array.getFloat(styleable, spreadFactor.toFloat()).toDouble()
                }
                R.styleable.CarouselRecyclerView_activityColor -> {
                    activeColor = array.getColor(styleable, activeColor)
                }
                R.styleable.CarouselRecyclerView_inactiveColorColor -> {
                    inactiveColor = array.getColor(styleable, inactiveColor)
                }
                R.styleable.CarouselRecyclerView_referenced_ids -> {
                    val referenceIds = array.getString(styleable)
                    val ids = ArrayList<Int>()
                    referenceIds?.let {
                        var begin = 0
                        while (true) {
                            val end = it.indexOf(',', begin)
                            if (end == -1) {
                                ids.add(findIdBy(it.substring(begin)))
                                return@let
                            }
                            ids.add(findIdBy(it.substring(begin, end)))
                            begin = end + 1
                        }
                    }
                    println(Arrays.toString(ids.toArray()))
                    viewsToChangeColor = ids
                }
            }
        }

        array.recycle()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        getAdapter()?.unregisterAdapterDataObserver(observer)
        adapter?.registerAdapterDataObserver(observer)
        super.setAdapter(adapter)
    }

    private fun findIdBy(name: String) = resources.getIdentifier(name, "id", context.packageName)

    fun setViewsToChangeColor(viewIds: List<Int>) {
        viewsToChangeColor = viewIds
    }

    private fun onScrollChanged() {
        post {
            (0 until childCount).forEach {
                val child = getChildAt(it)
                val childCenterX = (child.left + child.right) / 2
                val scale = gaussian(childCenterX)
                child.scaleX = scale
                child.scaleY = scale
                colorView(child, scale)
            }
        }
    }

    private fun colorView(child: View, scaleValue: Float) {
        val saturation = (scaleValue - 1) / 1f
        val alpha = scaleValue / 2f
        val matrix = ColorMatrix()
        matrix.setSaturation(saturation)

        viewsToChangeColor.forEach { viewId ->
            when (val viewToChangeColor = child.findViewById<View>(viewId)) {
                is ImageView -> {
                    viewToChangeColor.colorFilter = ColorMatrixColorFilter(matrix)
                    viewToChangeColor.imageAlpha = (255 * alpha).toInt()
                }
                is TextView -> {
                    val textColor = ArgbEvaluator().evaluate(saturation, inactiveColor, activeColor)
                    viewToChangeColor.setTextColor(textColor as Int)
                }
            }
        }
    }

    /**
     * 一维高斯函数
     * f(x)=ae^{{-(x-b)^{2}/2c^{2}}}
     */
    private fun gaussian(childCenterX: Int): Float {
        val recyclerCenterX = (left + right) / 2
        return (Math.E.pow(-(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(2.toDouble()))) * scaleFactor + scaleOffset).toFloat()
    }
}