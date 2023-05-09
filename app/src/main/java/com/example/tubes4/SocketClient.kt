package com.example.tubes4

import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SocketClient(private val address: String, private val port: Int) {

    private var socket: Socket? = null
    private var output: OutputStreamWriter? = null

    fun connect(onConnected: () -> Unit, onFailure: (Exception) -> Unit) {
        Thread {
            try {
                socket = Socket(address, port)
                output = OutputStreamWriter(socket?.getOutputStream())
                onConnected()
            } catch (e: Exception) {
                onFailure(e)
            }
        }.start()
    }

    fun disconnect() {
        try {
            output?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendXYZ(x: Int, y: Int, z: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                output?.apply {
                    write("$x,$y,$z\n")
                    flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
