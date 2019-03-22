package com.github.waterpeak.loopviewpagerhelper

import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.lang.ref.WeakReference
import java.util.*

class LoopViewPagerHelper(
    private val viewPager: ViewPager,
    private val adapter: LoopViewPagerAdapter
) : PagerAdapter(),
    ViewPager.OnPageChangeListener {

    interface LoopViewPagerAdapter {
        fun getItemCount(): Int
        fun onCreateView(context: Context, position: Int): View
        fun onRemoveView(itemView: View)
    }

    abstract class CachedLoopViewPagerAdapter : LoopViewPagerAdapter {

        private val cache = LinkedList<View>()

        override fun onCreateView(context: Context, position: Int): View {
            val view = if(cache.isNotEmpty())cache.removeFirst() else createView(context, position)
            bindView(view, position)
            return view
        }

        override fun onRemoveView(itemView: View) {
            cache.add(itemView)
        }

        abstract fun createView(context: Context, position: Int): View
        abstract fun bindView(view: View, position: Int)
    }

    private class AutoPlayHandler(out: LoopViewPagerHelper) : Handler() {

        private val out = WeakReference(out)

        override fun handleMessage(msg: Message) {
            out.get()?.apply {
                if (stateIdle) {
                    nextPage()
                }
                if (autoPlay) {
                    sendEmptyMessageDelayed(0, autoPlayDelay)
                }
            }
        }
    }

    var onBannerChangeListener: ((index: Int) -> Unit)? = null
    var stateIdle = true
    var autoPlay = false
        set(value) {
            if (value != field) {
                if (value) {
                    val handler = autoPlayHandler ?: AutoPlayHandler(this).also { autoPlayHandler = it }
                    if (!handler.hasMessages(1)) {
                        handler.sendEmptyMessageDelayed(1, autoPlayDelay)
                    }
                } else {
                    autoPlayHandler?.removeMessages(1)
                }
                field = value
            }
        }

    private var autoPlayHandler: AutoPlayHandler? = null

    var autoPlayDelay: Long = 3000L

    init {
        viewPager.adapter = this
        viewPager.addOnPageChangeListener(this)
        if (adapter.getItemCount() > 1) {
            viewPager.setCurrentItem(1, false)
        }
    }

    fun nextPage() {
        val realCount = adapter.getItemCount()
        if (realCount > 1) {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }

    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun getCount(): Int {
        val realCount = adapter.getItemCount()
        return when (realCount) {
            0, 1 -> realCount
            else -> realCount + 2
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val realCount = adapter.getItemCount()
        val view = adapter.onCreateView(
            container.context, if (realCount > 1) {
                when (position) {
                    0 -> realCount - 1
                    realCount + 1 -> 0
                    else -> position - 1
                }
            } else position
        )
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
        adapter.onRemoveView(obj)
    }

    override fun onPageScrollStateChanged(state: Int) {
        stateIdle = state == ViewPager.SCROLL_STATE_IDLE
        if (stateIdle) {
            val position = viewPager.currentItem
            val realCount = adapter.getItemCount()
            if (realCount > 1) {
                if (position == 0) {
                    viewPager.setCurrentItem(realCount, false)
                } else if (position == realCount + 1) {
                    viewPager.setCurrentItem(1, false)
                }
            }
        }

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        onBannerChangeListener?.apply {
            val realCount = adapter.getItemCount()
            if (realCount > 1) {
                when (position) {
                    0 -> invoke(realCount - 1)
                    realCount + 1 -> invoke(0)
                    else -> invoke(position - 1)
                }
            } else {
                invoke(position)
            }
        }
    }

    val currentPosition: Int
        get() {
            val realCount = adapter.getItemCount()
            val position = viewPager.currentItem
            return if (realCount > 1) {
                when (position) {
                    0 -> realCount - 1
                    realCount + 1 -> 0
                    else -> position - 1
                }
            } else {
                position
            }
        }
}




