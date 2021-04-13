package com.originalstocks.scannerdemo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.originalstocks.scannerdemo.databinding.ActivityMainBinding
import com.originalstocks.scannerdemo.scanner.ScannerConstants.START_SCANNING_CMD
import com.originalstocks.scannerdemo.scanner.ScannerConstants.STOP_SCANNING_CMD
import java.util.*


class MainActivity : AppCompatActivity(), SerialInputOutputManager.Listener {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private val ACTION_USB_PERMISSION = "com.originalstocks.scannerdemo.USB_PERMISSION"
    private val barcode = StringBuffer()

    // controlling :
    var mPermissionIntent: PendingIntent? = null
    var mUsbManager: UsbManager? = null
    var mDevice: UsbDevice? = null
    private var mEndPoint: UsbEndpoint? = null
    private var detachReceiver: BroadcastReceiver? = null
    private var mInterface: UsbInterface? = null
    private var mConnection: UsbDeviceConnection? = null
    private val forceClaim = true
    var mDeviceList: HashMap<String, UsbDevice>? = null
    var protocol = 0

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            mInterface = device.getInterface(0)
                            mEndPoint = mInterface!!.getEndpoint(0) // 0 IN and  1 OUT to printer.
                            mConnection = mUsbManager!!.openDevice(device)
                            showToast(context, "PERMISSION GRANTED FOR THIS DEVICE")

                        }
                    } else {
                        Log.e(TAG, "onReceive_permissions denied")
                        showToast(context, "PERMISSION DENIED FOR THIS DEVICE")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (isRootedDevice(applicationContext)) {
            showToast(this, "Device is ROOTED")
        } else {
            showToast(this, "Device is NOT rooted.")
        }
        binding.scannedOutputText.text = "Scan QR to display data"

        stockMethodForFindingUSBDevices()

        libraryBasedImplementation()
        binding.powerOnButton.setOnClickListener {
        }

        binding.powerOffButton.setOnClickListener {
        }

    }

    private fun libraryBasedImplementation() {
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device)
        if (connection != null) {
            mPermissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                0
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(mUsbReceiver, filter)
            mUsbManager?.requestPermission(mDevice, mPermissionIntent)
        } else {
            Log.e(TAG, "checkDrivers_status_connection is NULL")
            return
        }

        val port = driver.ports[0] // Most devices have just one port (port 0)
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        val powerByteArray: ByteArray = STOP_SCANNING_CMD.toByteArray()
        binding.powerOffButton.setOnClickListener {
            showToast(this, "Sending command to stop scanning")
            port.write(powerByteArray, 2000)
        }

    }

    private fun stockMethodForFindingUSBDevices() {
        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mDeviceList = mUsbManager?.deviceList

        Log.i(TAG, "onCreate_device_list = $mDeviceList")
        if (mDeviceList?.size == 0) {
            Log.e(TAG, "USB device not found")
            showToast(this, "Please attach a USB device")
            return
        } else {
            val mDeviceIterator: Iterator<UsbDevice>? = mDeviceList?.values?.iterator()
            showToast(this, "Device List Size: ${mDeviceList?.size}")
            var usbDevice = ""



            while (mDeviceIterator!!.hasNext()) {
                val usbDevice1: UsbDevice = mDeviceIterator.next()
                usbDevice += """
                            DeviceID: ${usbDevice1.deviceId}
                            DeviceName: ${usbDevice1.deviceName}
                            Protocol: ${usbDevice1.deviceProtocol}
                            Product Name: ${usbDevice1.productName}
                            Manufacturer Name: ${usbDevice1.manufacturerName}
                            DeviceClass: ${usbDevice1.deviceClass} - ${
                    translatedDeviceClass(
                        usbDevice1.deviceClass
                    )
                }
                            DeviceSubClass: ${usbDevice1.deviceSubclass}
                            VendorID: ${usbDevice1.vendorId}
                            ProductID: ${usbDevice1.productId}
                            """
                val interfaceCount = usbDevice1.interfaceCount
                showToast(this, "INTERFACE COUNT: $interfaceCount")

                protocol = usbDevice1.deviceProtocol
                mDevice = usbDevice1
                showToast(this, "Device is attached")
                binding.deviceCount.text = usbDevice
                Log.i(TAG, "onCreate_device_info = $usbDevice")
            }

            mPermissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                0
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(mUsbReceiver, filter)
            mUsbManager?.requestPermission(mDevice, mPermissionIntent)

            //sendCommandsToUSBScanner(mConnection, mInterface)
            mConnection?.claimInterface(mInterface, forceClaim)

            binding.powerOnButton.setOnClickListener {
                powerUSBScanner(true, mConnection!!, mEndPoint)
            }

            binding.powerOffButton.setOnClickListener {
                powerUSBScanner(false, mConnection!!, mEndPoint)
            }
        }
    }

    private fun powerUSBScanner(
        isToPowerOn: Boolean,
        connection: UsbDeviceConnection,
        mEndPoint: UsbEndpoint?
    ) {
        val powerByteArray: ByteArray = if (isToPowerOn) {
            showToast(this, "Sending command to START scanning")
            START_SCANNING_CMD.toByteArray()
        } else {
            showToast(this, "Sending command to stop scanning")
            STOP_SCANNING_CMD.toByteArray()
        }
        val thread = Thread {
            connection.bulkTransfer(
                mEndPoint,
                powerByteArray,
                powerByteArray.size,
                2000
            )
        }
        thread.run()
    }

    private fun translatedDeviceClass(deviceClass: Int): String {
        return when (deviceClass) {
            UsbConstants.USB_CLASS_APP_SPEC -> "Application specific USB class"
            UsbConstants.USB_CLASS_AUDIO -> "USB class for audio devices"
            UsbConstants.USB_CLASS_CDC_DATA -> "USB class for CDC devices (communications device class)"
            UsbConstants.USB_CLASS_COMM -> "USB class for communication devices"
            UsbConstants.USB_CLASS_CONTENT_SEC -> "USB class for content security devices"
            UsbConstants.USB_CLASS_CSCID -> "USB class for content smart card devices"
            UsbConstants.USB_CLASS_HID -> "USB class for human interface devices (for example, mice and keyboards)"
            UsbConstants.USB_CLASS_HUB -> "USB class for USB hubs"
            UsbConstants.USB_CLASS_MASS_STORAGE -> "USB class for mass storage devices"
            UsbConstants.USB_CLASS_MISC -> "USB class for wireless miscellaneous devices"
            UsbConstants.USB_CLASS_PER_INTERFACE -> "USB class indicating that the class is determined on a per-interface basis"
            UsbConstants.USB_CLASS_PHYSICA -> "USB class for physical devices"
            UsbConstants.USB_CLASS_PRINTER -> "USB class for printers"
            UsbConstants.USB_CLASS_STILL_IMAGE -> "USB class for still image devices (digital cameras)"
            UsbConstants.USB_CLASS_VENDOR_SPEC -> "Vendor specific USB class"
            UsbConstants.USB_CLASS_VIDEO -> "USB class for video devices"
            UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "USB class for wireless controller devices"
            else -> "Unknown USB class!"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(mUsbReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** This function returns scanned value. */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            Log.i(TAG, "dispatchKeyEvent: $event")
            val pressedKey = event.unicodeChar.toChar()
            barcode.append(pressedKey)
        }
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
            showToast(this, "barcode--->>>$barcode")
            Log.e(TAG, "dispatchKeyEvent_barcode--->>>$barcode")
            binding.scannedOutputText.text = "Scanned Data : $barcode"
            barcode.delete(0, barcode.length)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onNewData(data: ByteArray?) {
        Log.e(TAG, "onNewData_data = $data")
    }

    override fun onRunError(e: java.lang.Exception?) {
        Log.e(TAG, "onRunError_exception = ${e?.printStackTrace()}")
    }

}