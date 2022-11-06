package com.dmytro.nfcwallet

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NfcReaderActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_reader_activity)
        println("NFC ACTIVITY OPENED")

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
        var isNfcSupported: Boolean =
            this.nfcAdapter != null
        if (!isNfcSupported) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(
                this,
                "NFC disabled on this device. Turn on to proceed",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }


    override fun onNewIntent(intent: Intent?) {
        println("NFC TEST")
        super.onNewIntent(intent)
        var tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val nfc = NfcA.get(tagFromIntent)
        nfc.connect()
        val isConnected = nfc.isConnected()
        /*val GET_STRING = byteArrayOf(
            0x80.toByte(),  //CLA Class
            0x04,  //INS Instruction
            0x00,  //P1  Parameter 1
            0x00,  //P2  Parameter 2
            0x10 //LE  maximal number of bytes expected in result
        )*/
        println("isconencted: "+ isConnected.toString())
        if (isConnected) {
            val receivedData: ByteArray = nfc.transceive(byteArrayOf(
                0x30.toByte(),  /* CMD = READ */
                0x10.toByte() /* PAGE = 16  */
            ))
            val len: Int = receivedData.size
            if (!(receivedData.get(len - 2) === 0x90.toByte() && receivedData.get(len - 1) === 0x00.toByte())) throw RuntimeException(
                "could not retrieve msisdn"
            )

            val data = ByteArray(len - 2)
            System.arraycopy(receivedData, 0, data, 0, len - 2)
            val str = String(data).trim { it <= ' ' }
            println("nfc tag:"+str)
            //code to handle the received data
            // Received data would be in the form of a byte array that can be converted to string
            //NFC_READ_COMMAND would be the custom command you would have to send to your NFC Tag in order to read it
        } else {
            Log.e("ans", "Not connected")
        }
    }
    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {

        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()

        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType("text/plain")
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                val e = "ERROR"
                throw RuntimeException(e)
            }
        }

        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }
    override fun onResume() {
        super.onResume()

        enableForegroundDispatch(this, this.nfcAdapter)

    }
}
