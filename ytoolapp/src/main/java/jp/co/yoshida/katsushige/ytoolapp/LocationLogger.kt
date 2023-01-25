package jp.co.yoshida.katsushige.ytoolapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import jp.co.yoshida.katsushige.ytoolapp.databinding.ActivityLocationLoggerBinding

//  https://dev.classmethod.jp/articles/android-use-foreground-service-for-location-background/

class LocationLogger : AppCompatActivity() {

    companion object {
        val TAG = "LocationLogger"
        private const val PERMISSION_REQUEST_CODE = 1234
    }

    lateinit var binding: ActivityLocationLoggerBinding
//    lateinit var constraintLayout: ConstraintLayout
//    lateinit var linearLayoutMap: LinearLayout
    lateinit var btStart: Button
    lateinit var btFinish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_logger)
        this.title = "位置情報ロガー"

        binding = ActivityLocationLoggerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btStart = binding.startButton
        btFinish = binding.finishButton

        requestPermission()

        btStart.setOnClickListener {

        }

        btFinish.setOnClickListener {

        }
    }

    private fun requestPermission() {
        val permissionAccessCoarseLocationApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            if (backgroundLocationPermissionApproved) {
                //  フォアグランドとバックグランドのパーミッションがある
            } else {
                //  フォアグランドのみOKなのでバックグラウンドの許可を求める
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_REQUEST_CODE)
            }
        } else {
            //  位置情報の権限がない時の許可を求める
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                PERMISSION_REQUEST_CODE)
        }
    }
}