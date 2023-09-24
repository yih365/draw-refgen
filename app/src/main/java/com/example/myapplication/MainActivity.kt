package com.example.myapplication

import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
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
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            350
        )
        parent.addView(myDrawView)

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
            val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            Log.d(TAG, encoded)

            // Make api call
            val PROJECT_ID = "draw-caption-ref"
            val URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/${PROJECT_ID}/locations/us-central1/publishers/google/models/imagetext:predict"
            val client = AsyncHttpClient()
            val params = RequestParams()
            params["sampleCount"] = "1"
            params["image"] = encoded
//            params["language"] = "en"
            client.get(URL, params, object: JsonHttpResponseHandler() {
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
                    Log.d(TAG, "success")
                    Log.d(TAG, json.toString())
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

//        // Add text
//        val captionText = TextView(this)
//        captionText.id = View.generateViewId()
//

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
        constraintSet.applyTo(parent)
    }

    private fun getFileName(): String {
        return "drawingFile.png"
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
