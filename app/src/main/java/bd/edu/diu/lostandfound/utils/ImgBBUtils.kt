package bd.edu.diu.lostandfound.utils

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ImgBBUtils {

    const val API_KEY = "14a16e6f1a12ca0ec2ab446df6e38a09"

    fun uploadImageToImgBB(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        
        Thread {
            try {
                // Read bytes from selected Uri
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null || bytes.isEmpty()) {
                    mainHandler.post { callback(null) }
                    return@Thread
                }

                // Base64 encode the image bytes
                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

                // ImgBB Upload Endpoint
                val urlString = "https://api.imgbb.com/1/upload?key=$API_KEY"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true
                connection.connectTimeout = 12000
                connection.readTimeout = 12000
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "image=" + URLEncoder.encode(base64Image, "UTF-8")

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(postData)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonResponse = JSONObject(response.toString())
                    if (jsonResponse.optBoolean("success")) {
                        val dataObj = jsonResponse.getJSONObject("data")
                        val imageUrl = dataObj.optString("display_url", dataObj.optString("url"))
                        mainHandler.post { callback(imageUrl) }
                        return@Thread
                    }
                }
                mainHandler.post { callback(null) }
            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post { callback(null) }
            }
        }.start()
    }
}
