package com.qifan.hooktest

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import java.lang.reflect.Field
import java.lang.reflect.Method

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.hook_onclick)
        button.setOnClickListener { Log.d("Button", "Origin click") }
        try {
            hookOnClickListener(button)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    @Throws(Exception::class)
    fun hookOnClickListener(view: View) {
        // 第一步：反射得到 ListenerInfo 对象
        val getListenerInfo: Method = View::class.java.getDeclaredMethod("getListenerInfo")
        getListenerInfo.isAccessible = true
        val listenerInfo: Any = getListenerInfo.invoke(view) as Any
        // 第二步：得到原始的 OnClickListener事件方法
        val listenerInfoClz: Class<*> = Class.forName("android.view.View\$ListenerInfo")
        val mOnClickListener: Field = listenerInfoClz.getDeclaredField("mOnClickListener")
        mOnClickListener.isAccessible = true
        val originOnClickListener: View.OnClickListener = mOnClickListener.get(listenerInfo) as View.OnClickListener
        // 第三步：用 Hook代理类 替换原始的 OnClickListener
        val hookedOnClickListener: View.OnClickListener = HookedClickListenerProxy(originOnClickListener)
        mOnClickListener.set(listenerInfo, hookedOnClickListener)
    }


    class HookedClickListenerProxy(private val origin: View.OnClickListener?) :
        View.OnClickListener {
        override fun onClick(view: View) {
            Toast.makeText(view.context, "Hook onclick listener", Toast.LENGTH_LONG).show()
            origin?.onClick(view)
        }
    }
}