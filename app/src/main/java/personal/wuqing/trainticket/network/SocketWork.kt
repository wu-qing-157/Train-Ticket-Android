package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

object SocketWork {
    private const val host = "dhc.moe"
    private const val port = 8081
    fun getResult(s: CharSequence): Result<String> {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port) as SocketAddress)
            socket.getOutputStream().write(s.toString().toByteArray())
            socket.shutdownOutput()
            val input = socket.getInputStream()
            val bytes = ByteArray(1048576)
            val len = input.read(bytes)
            Result.Success(String(bytes, 0, len))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}