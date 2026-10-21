package bd.edu.diu.lostandfound

import android.app.Application
import com.google.firebase.FirebaseApp

class DIUApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
