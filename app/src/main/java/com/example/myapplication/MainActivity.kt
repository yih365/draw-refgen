package com.example.myapplication

import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.bumptech.glide.Glide
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestHeaders
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add draw view
        val parent = findViewById<ConstraintLayout>(R.id.main)
        val myDrawView = MyDrawView(this)
        myDrawView.setDrawingCacheEnabled(true);
        myDrawView.id = View.generateViewId()
        myDrawView.layoutParams = ConstraintLayout.LayoutParams(
            512,
           512
        )
        val border = GradientDrawable()
        border.setColor(-0x1) //white background
        border.setStroke(1, -0x1000000) //black border with full opacity
        myDrawView.background = border
        parent.addView(myDrawView)

        // Add view for image ref
        val imageRefView = ImageView(this)
        imageRefView.id = View.generateViewId()
        parent.addView(imageRefView)

        // Add button
        val button = Button(this)
        button.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        button.text = "Submit"
        button.id = View.generateViewId()
        button.setOnClickListener {
            // Base64 encode image
            val byteArrayOutputStream = ByteArrayOutputStream()
            myDrawView.drawingCache.compress(CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

            val n = 3
            val body: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "drawing.png", byteArray.toRequestBody("image/png".toMediaTypeOrNull()))
                .addFormDataPart("n", n.toString())
                .addFormDataPart("size", "512x512")
                .build()

            // Make api call
            val URL = "https://api.openai.com/v1/images/variations"
            val client = AsyncHttpClient()
            val params = RequestParams()
            val requestHeaders = RequestHeaders()
            // TODO: hide this
            val API_KEY = "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"
            requestHeaders["Authorization"] = "Bearer $API_KEY"
            requestHeaders["Content-Type"] = "multipart/form-data"

            client.post(URL, requestHeaders, params, body, object: JsonHttpResponseHandler() {
                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.e(TAG, statusCode.toString())
                    if (response != null) {
                        Log.e(TAG, "failed to get caption $response")
                    }
                }

                override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                    Log.d(TAG, json.toString())

                    val url = json?.jsonObject?.getJSONArray("data")?.getJSONObject(0)?.getString("url")
                    Log.d(TAG, "url is $url")

                    var i = 0
                    imageRefView.setOnClickListener{
                        val clickUrl = json?.jsonObject?.getJSONArray("data")?.getJSONObject(i)?.getString("url")
                        Glide.with(this@MainActivity).load(clickUrl).fitCenter().override(512).into(imageRefView)
                        i += 1
                        if (i==n) {
                            i = 0
                        }
                    }

                    Glide.with(this@MainActivity).load(url).fitCenter().override(512).into(imageRefView)
                }
            })
//            var fos: FileOutputStream? = null
//            try {
////                fos = FileOutputStream(getFileName())
//                fos = openFileOutput(getFileName(), Context.MODE_PRIVATE)
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            }
//            myDrawView.drawingCache.compress(CompressFormat.PNG, 95, fos)
        }
        parent.addView(button)

        // Set position of view
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)
        constraintSet.connect(
            myDrawView.id,
            ConstraintSet.BOTTOM,
            parent.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            myDrawView.id, ConstraintSet.TOP,
            button.id, ConstraintSet.BOTTOM, 0
        )
        constraintSet.connect(
            imageRefView.id, ConstraintSet.LEFT,
            button.id, ConstraintSet.RIGHT, 10
        )
        constraintSet.connect(
            imageRefView.id, ConstraintSet.RIGHT,
            parent.id, ConstraintSet.RIGHT, 10
        )
        constraintSet.connect(
            imageRefView.id, ConstraintSet.TOP,
            parent.id, ConstraintSet.TOP, 10
        )
        constraintSet.applyTo(parent)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
