package com.github.waterpeak.loopviewpagerhelper.example

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.ViewPager
import com.github.waterpeak.loopviewpagerhelper.LoopViewPagerHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val pager = ViewPager(this)
        val adapter = LoopViewPagerHelper(
            pager,
            object : LoopViewPagerHelper.CachedLoopViewPagerAdapter() {
                override fun getItemCount(): Int = 3

                override fun createView(context: Context, position: Int): View =
                    AppCompatTextView(this@MainActivity).apply {
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 20f
                    }

                override fun bindView(view: View, position: Int) {
                    (view as TextView).apply {
                        text = position.toString()
                        setBackgroundColor(
                            when (position) {
                                0 -> Color.RED
                                1 -> Color.GREEN
                                else -> Color.BLUE
                            }
                        )
                    }
                }

            })

        linearLayout.addView(pager, LinearLayout.LayoutParams(MATCH_PARENT, 200.dip))
        val textView = AppCompatTextView(this).apply {
            text = "Other content"
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            textSize = 20f
            setBackgroundColor(Color.BLACK)
        }
        linearLayout.addView(textView, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        setContentView(linearLayout)
        //自动播放
        adapter.autoPlay = true
    }

    private val Int.dip: Int
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics).toInt()
}