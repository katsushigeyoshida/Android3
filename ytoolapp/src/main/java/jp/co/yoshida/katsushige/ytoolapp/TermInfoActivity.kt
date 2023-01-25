package jp.co.yoshida.katsushige.ytoolapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.yoshida.katsushige.ytoolapp.databinding.ActivityTermInfoBinding


class TermInfoActivity : AppCompatActivity() {

    val TAG = "TermInfoActivity"

    //  ViewBinding (bundle.gradleにbuildFeaturesを追加)
    lateinit var binding: ActivityTermInfoBinding
    lateinit var constraintLayout: ConstraintLayout
    lateinit var linearLayoutMap: LinearLayout
    lateinit var btApplication: Button
    lateinit var btHardware: Button
    lateinit var btDisplayInfo: Button
    lateinit var btSensor: Button
    lateinit var tvList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_info)
        title = "端末情報"

        binding = ActivityTermInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constrainLayout
        btApplication = binding.button5
        btHardware = binding.button6
        btDisplayInfo = binding.button7
        btSensor = binding.button8
        tvList = binding.textView22

        init()

        tvList.text = ""
        tvList.movementMethod = ScrollingMovementMethod()
    }

    fun init() {
        btApplication.setOnClickListener {
            tvList.text = getApplications()
        }

        btHardware.setOnClickListener {
            tvList.text = getHardware()
        }

        btHardware.setOnLongClickListener {
            tvList.text = getHardware(false)
            true
        }

        btDisplayInfo.setOnClickListener {
            tvList.text = getDisplayInfo() + "\n" + getSystemInfo()
        }

        btSensor.setOnClickListener {
            tvList.text = getSensorList()
        }

        btSensor.setOnLongClickListener {
            Log.d(TAG, "setOnLongClickListener:")
            val intent = Intent(this, SensorActivity::class.java)
            startActivity(intent)
            true
        }
    }

    fun getApplications(): String {
        var appli = "Application\n"
        val pm = packageManager
        val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (i in list.indices) {
            appli += "${i}: ${list[i].packageName}\n"
        }
        return  appli
    }

    fun getHardware(enable: Boolean = true): String{
        val hwSystemFeature = arrayOf(
            PackageManager.FEATURE_APP_WIDGETS,
            "The device supports app widgets",
            PackageManager.FEATURE_AUDIO_LOW_LATENCY,
            "OpenSL ESを使用した音声処理に対応",
            PackageManager.FEATURE_AUDIO_OUTPUT,
            "The device includes at least one form of audio output, such as speakers, audio jack or streaming over bluetooth ",
            PackageManager.FEATURE_AUDIO_PRO,
            "The device has professional audio level of functionality and performance. ",
            PackageManager.FEATURE_AUTOMOTIVE,
            "This is a device dedicated to showing UI on a vehicle headunit. ",
            PackageManager.FEATURE_BACKUP,
            "The device can perform backup and restore operations on installed applications. ",
            PackageManager.FEATURE_BLUETOOTH,
            "Bluetooth",
            PackageManager.FEATURE_BLUETOOTH_LE,
            "The device is capable of communicating with other devices via Bluetooth Low Energy radio. ",
            PackageManager.FEATURE_CAMERA,
            "スクリーンの反対側のカメラ",
            PackageManager.FEATURE_CAMERA_ANY,
            "The device has at least one camera pointing in some direction, or can support an external camera being connected to it.",
            PackageManager.FEATURE_CAMERA_AUTOFOCUS,
            "カメラのオートフォーカス機能",
            PackageManager.FEATURE_CAMERA_CAPABILITY_MANUAL_POST_PROCESSING,
            "At least one of the cameras on the device supports the manual post-processing capability level.",
            PackageManager.FEATURE_CAMERA_CAPABILITY_MANUAL_SENSOR,
            "At least one of the cameras on the device supports the manual sensor capability level.",
            PackageManager.FEATURE_CAMERA_CAPABILITY_RAW,
            "At least one of the cameras on the device supports the RAW capability level.",
            PackageManager.FEATURE_CAMERA_EXTERNAL,
            "The device can support having an external camera connected to it. ",
            PackageManager.FEATURE_CAMERA_FLASH,
            "カメラのフラッシュ",
            PackageManager.FEATURE_CAMERA_FRONT,
            "スクリーン側の内向きカメラ",
            PackageManager.FEATURE_CAMERA_LEVEL_FULL,
            "At least one of the cameras on the device supports the full hardware capability level.",
            PackageManager.FEATURE_CONNECTION_SERVICE,
            "The Connection Service API is enabled on the device.",
            PackageManager.FEATURE_CONSUMER_IR,
            "The device is capable of communicating with consumer IR devices.",
            PackageManager.FEATURE_DEVICE_ADMIN,
            "The device supports device policy enforcement via device admins.",
            PackageManager.FEATURE_ETHERNET,
            "This device supports ethernet.",
            PackageManager.FEATURE_FAKETOUCH,
            "The device does not have a touch screen, but does support touch emulation for basic events.",
            PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT,
            "he device does not have a touch screen, but does support touch emulation for basic events that supports distinct tracking of two or more fingers.",
            PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND,
            "The device does not have a touch screen, but does support touch emulation for basic events that supports tracking a hand of fingers (5 or more fingers) fully independently.",
            PackageManager.FEATURE_FINGERPRINT,
            "The device has biometric hardware to detect a fingerprint.",
            PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT,
            "The device supports freeform window management.",
            PackageManager.FEATURE_GAMEPAD,
            "The device has all of the inputs necessary to be considered a compatible game controller, or includes a compatible game controller in the box. ",
            PackageManager.FEATURE_HIFI_SENSORS,
            "The device supports high fidelity sensor processing capabilities. ",
            PackageManager.FEATURE_HOME_SCREEN,
            "The device supports a home screen that is replaceable by third party applications. ",
            PackageManager.FEATURE_INPUT_METHODS,
            "The device supports adding new input methods implemented with the InputMethodService API. ",
            PackageManager.FEATURE_LEANBACK,
            "The device supports leanback UI. ",
            PackageManager.FEATURE_LIVE_TV,
            "The device supports live TV and can display contents from TV inputs implemented with the TvInputService API. ",
            PackageManager.FEATURE_LIVE_WALLPAPER,
            "ライブ壁紙のサポート",
            PackageManager.FEATURE_LOCATION,
            "1つ以上の位置情報取得機能",
            PackageManager.FEATURE_LOCATION_GPS,
            "GPS(Global Positioning System",
            PackageManager.FEATURE_LOCATION_NETWORK,
            "ネットワークベースの位置情報取得",
            PackageManager.FEATURE_MANAGED_USERS,
            "The device supports creating secondary users and managed profiles via DevicePolicyManager. ",
            PackageManager.FEATURE_MICROPHONE,
            "マイクによる音声入力",
            PackageManager.FEATURE_MIDI,
            "The device has a full implementation of the android.media.midi.* APIs. ",
            PackageManager.FEATURE_NFC,
            "NFCの対応",
            PackageManager.FEATURE_NFC_HOST_CARD_EMULATION,
            "The device supports host- based NFC card emulation. ",
            PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF,
            "The device supports host- based NFC-F card emulation.",
            PackageManager.FEATURE_OPENGLES_EXTENSION_PACK,
            "The device supports the OpenGL ES Android Extension Pack.",
            PackageManager.FEATURE_PICTURE_IN_PICTURE,
            "The device supports picture-in-picture multi-window mode.",
            PackageManager.FEATURE_PRINTING,
            "The device supports printing.",
            PackageManager.FEATURE_SCREEN_LANDSCAPE,
            "The device supports landscape orientation screens. ",
            PackageManager.FEATURE_SCREEN_PORTRAIT,
            "The device supports portrait orientation screens.",
            PackageManager.FEATURE_SECURELY_REMOVES_USERS,
            "The device supports secure removal of users. ",
            PackageManager.FEATURE_SENSOR_ACCELEROMETER,
            "加速度センサー",
            PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE,
            "The device includes an ambient temperature sensor.",
            PackageManager.FEATURE_SENSOR_BAROMETER,
            "圧力センサー",
            PackageManager.FEATURE_SENSOR_COMPASS,
            "コンパス(磁気センサー)",
            PackageManager.FEATURE_SENSOR_GYROSCOPE,
            "ジャイロスコープ",
            PackageManager.FEATURE_SENSOR_HEART_RATE,
            "The device includes a heart rate monitor. ",
            PackageManager.FEATURE_SENSOR_HEART_RATE_ECG,
            "The heart rate sensor on this device is an Electrocardiogram.",
            PackageManager.FEATURE_SENSOR_LIGHT,
            "照度センサー",
            PackageManager.FEATURE_SENSOR_PROXIMITY,
            "近接センサー",
            PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY,
            "The device includes a relative humidity sensor. ",
            PackageManager.FEATURE_SENSOR_STEP_COUNTER,
            "The device includes a hardware step counter.",
            PackageManager.FEATURE_SENSOR_STEP_DETECTOR,
            "The device includes a hardware step detector.",
            PackageManager.FEATURE_SIP,
            "SIP APIの対応",
            PackageManager.FEATURE_SIP_VOIP,
            "SIPベースのVOIP(ip電話)に対応",
            PackageManager.FEATURE_TELEPHONY,
            "移動体通信網によるデータ通信",
            PackageManager.FEATURE_TELEPHONY_CDMA,
            "CDMAネットワーク",
            PackageManager.FEATURE_TELEPHONY_GSM,
            "GSMネットワーク",
            PackageManager.FEATURE_TOUCHSCREEN,
            "タッチスクリーン",
            PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH,
            "2点のマルチタッチスクリーン",
            PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT,
            "2点以上のマルチタッチスクリーン",
            PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND,
            "5点以上のマルチタッチスクリーン",
            PackageManager.FEATURE_USB_ACCESSORY,
            "The device supports connecting to USB",
            PackageManager.FEATURE_USB_HOST,
            "The device supports connecting to USB devices as the USB host. ",
            PackageManager.FEATURE_VERIFIED_BOOT,
            "The device supports verified boot. ",
            PackageManager.FEATURE_VR_MODE,
            "The device implements an optimized mode for virtual reality (VR) applications that handles stereoscopic rendering of notifications, and disables most monocular system UI components while a VR application has user focus.",
            PackageManager.FEATURE_VR_MODE_HIGH_PERFORMANCE,
            "The device implements FEATURE_VR_MODE but additionally meets extra CDD requirements to provide a high-quality VR experience. ",
            PackageManager.FEATURE_VULKAN_HARDWARE_VERSION,
            "The version of this feature indicates the highest VkPhysicalDeviceProperties::apiVersion supported by the physical devices that support the hardware level indicated by FEATURE_VULKAN_HARDWARE_LEVEL. ",
            PackageManager.FEATURE_WATCH,
            "This is a device dedicated to showing UI on a watch.",
            PackageManager.FEATURE_WEBVIEW,
            "The device has a full implementation of the android.webkit.* APIs.",
            PackageManager.FEATURE_WIFI,
            "The device supports WiFi (802.11) networking. ",
            PackageManager.FEATURE_WIFI_DIRECT,
            "The device supports Wi-Fi Direct networking. "
        )

        var appli = "HardWareFeature\n"
        for (i in 0 until hwSystemFeature.size / 2) {
            val hasFeature = packageManager.hasSystemFeature(hwSystemFeature[i * 2])
            if (hasFeature || enable)
                appli += "${i}: ${hwSystemFeature[i * 2 + 1]} [$hasFeature] ${hwSystemFeature[i * 2]}\n"
        }
        return appli
    }

    fun getDisplayInfo(): String {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val wm = windowManager
        val display: Display = wm.defaultDisplay

        var buf: String
        buf = "端末画面情報\n"
        buf += "density (論理的密度) : " + "${metrics.density}\n"
        buf += "densityDpi (ドット数/インチ) : " + "${metrics.densityDpi}\n"
        buf += "widthPixels (幅) :    " + "${metrics.widthPixels}\n"
        buf += "heightPixels (高さ) : " + "${metrics.heightPixels}\n"
        buf += "xdpi (X軸ピクセル数/インチ) :   " + "${metrics.xdpi}\n"
        buf += "ydpi (Y軸ピクセル数/インチ) :   " + "${metrics.ydpi}\n"
        buf += "width :      " + "${java.lang.String.valueOf(display.getWidth())}\n"
        buf += "height:      " + "${java.lang.String.valueOf(display.getHeight())}\n"    // 非推奨
        buf += "orientation: " + "${java.lang.String.valueOf(display.getOrientation())}\n"  // 非推奨
        buf += "refreshRate: " + "${java.lang.String.valueOf(display.getRefreshRate())}\n"
        buf += "pixelFormat: " + "${java.lang.String.valueOf(display.getPixelFormat())}\n"
        buf += "rotation:    " + "${java.lang.String.valueOf(display.getRotation())}\n"

        return buf
    }

    fun getSystemInfo(): String {
        var buf = ""
        buf = "ボード(基盤)名称 : " +  Build.BOARD + "\n"
        buf += "ブートローダのバージョン: " + Build.BOOTLOADER + "\n"    //Android 1.6未対応
        buf += "ブランド名: " + Build.BRAND + "\n"
        buf += "ネイティブコードの命令セット: " + Build.CPU_ABI + "\n"
        buf += "ネイティブコードの第2命令セット: " + Build.CPU_ABI2 + "\n"        //Android 1.6未対応
        buf += "デバイス名: " + Build.DEVICE + "\n"
        buf += "ユーザへ表示するビルドID: " + Build.DISPLAY + "\n"
        buf += "ビルドを識別子: " + Build.FINGERPRINT + "\n"
        buf += "ハードウェア名: " + Build.HARDWARE + "\n"        //Android 1.6未対応
        buf += "ホスト名: " + Build.HOST + "\n"
        buf += "変更番号: " + Build.ID + "\n"
        buf += "製造者名: " + Build.MANUFACTURER + "\n"
        buf += "モデル名: " + Build.MODEL + "\n"
        buf += "製品名: " + Build.PRODUCT + "\n"
        buf += "無線ファームウェアのバージョン: " + Build.RADIO + "\n"              //Android 1.6未対応
        buf += "ビルドのタグ名:" + Build.TAGS + "\n"
        buf += "システム時刻: " + Build.TIME + "\n"
        buf += "ビルドタイプ: " + Build.TYPE + "\n"
        buf += "情報不明時の識別子: " + Build.UNKNOWN + "\n"          //Android 1.6未対応
        buf += "ユーザ情報: " + Build.USER + "\n"
        buf += "開発コードネーム: " + Build.VERSION.CODENAME + "\n"
        buf += "ソースコード管理番号: " + Build.VERSION.INCREMENTAL + "\n"
        buf += "バージョン番号: " + Build.VERSION.RELEASE + "\n"
        buf += "VERSION.SDK:" + Build.VERSION.SDK + "\n"
        buf += "フレームワークのバージョン情報: " + Build.VERSION.SDK_INT + "\n"
        return buf
    }

    fun getSensorList(): String {
        //  https://developer.android.com/reference/kotlin/android/hardware/Sensor
        val SensorType: HashMap<Int, String> = HashMap()
        SensorType[Sensor.TYPE_ACCELEROMETER] = "加速度"       //1
        SensorType[Sensor.TYPE_MAGNETIC_FIELD] = "地磁気"      //2
        SensorType[Sensor.TYPE_ORIENTATION] = "方位(非推奨)"     //3
        SensorType[Sensor.TYPE_GYROSCOPE] = "ジャイロ"          //4
        SensorType[Sensor.TYPE_LIGHT] = "光"                 //5
        SensorType[Sensor.TYPE_PRESSURE] = "気圧"             //6
        SensorType[Sensor.TYPE_TEMPERATURE] = "温度(非推奨)"     //7
        SensorType[Sensor.TYPE_PROXIMITY] = "近接"            //8
        SensorType[Sensor.TYPE_GRAVITY] = "重力"              //9
        SensorType[Sensor.TYPE_LINEAR_ACCELERATION] = "線形加速" //10
        SensorType[Sensor.TYPE_ROTATION_VECTOR] = "回転"          //11
        SensorType[Sensor.TYPE_RELATIVE_HUMIDITY] = "相対湿度"      //12
        SensorType[Sensor.TYPE_AMBIENT_TEMPERATURE] = "周囲温度"          //13
        SensorType[Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED] = "非校正磁界" //14
        SensorType[Sensor.TYPE_GAME_ROTATION_VECTOR] = "非校正回転"      //15
        SensorType[Sensor.TYPE_GYROSCOPE_UNCALIBRATED] = "非校正ジャイロ" //16
        SensorType[Sensor.TYPE_SIGNIFICANT_MOTION] = "モーショントリガ"     //17
        SensorType[Sensor.TYPE_STEP_DETECTOR] = "ステップ検出器"           //18
        SensorType[Sensor.TYPE_STEP_COUNTER] = "ステップカウンタ"           //19
        SensorType[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR] = "地磁気の回転" //	20
        SensorType[Sensor.TYPE_HEART_RATE] = "心拍数"                      //21
        SensorType[Sensor.TYPE_POSE_6DOF] = "姿勢"                        //28
        SensorType[Sensor.TYPE_STATIONARY_DETECT] = ""                  //29
        SensorType[Sensor.TYPE_MOTION_DETECT] = "動き検出"              //30
        SensorType[Sensor.TYPE_HEART_BEAT] = "心拍検出"                 //31
        SensorType[Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT] = "低遅延オフボディ検出" //34
        SensorType[Sensor.TYPE_ACCELEROMETER_UNCALIBRATED] = "非校正加速度" //35
        SensorType[Sensor.TYPE_DEVICE_PRIVATE_BASE] = "ベンダー定義"      //0x10000
        var buf = ""
        val mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val list: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in list) {
            buf += sensor.getType().toString() + ","
            buf += "[" + SensorType[sensor.getType()].toString() + "]"
            buf += sensor.getName() + ", "
            buf += sensor.getVendor() + "\n"
        }
        return buf
    }
}