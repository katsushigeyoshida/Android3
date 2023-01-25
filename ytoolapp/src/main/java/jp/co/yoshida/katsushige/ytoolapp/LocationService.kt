package jp.co.yoshida.katsushige.ytoolapp

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

//  https://dev.classmethod.jp/articles/android-use-foreground-service-for-location-background/
class LocationService : Service() {

    companion object {
        val TAG = "LocationService"
        const val CHANNEL_ID = "777"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var updateCount = 0
        locationCallback = object  : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
//                super.onLocationResult(p0)
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateCount++
                    Log.d(this.javaClass.name, "[${updateCount}] ${location.latitude},${location.longitude}")
                }

            }
        }
        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("位置情報テスト")
            .setContentText("位置情報を取得しています...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .build()

        startForeground(9999, notification)

        startLocationUpdates()

        return START_STICKY
//        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startLocationUpdates() {
//        val locationRequest = createLocationRequest() ?: return
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}