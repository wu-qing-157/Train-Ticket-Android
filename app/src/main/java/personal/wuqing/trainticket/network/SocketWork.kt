package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result
import java.lang.Exception
import java.net.Socket

object SocketWork {
    private const val host = "127.0.0.1"
    private const val port = 8081
    fun getResult(s: CharSequence, checker: Regex): Result<String> {
        return try {
            val socket = Socket(host, port)
            socket.getOutputStream().write(s.toString().toByteArray())
            socket.shutdownOutput()
            val input = socket.getInputStream()
            val bytes = ByteArray(1048576)
            val len = input.read(bytes)
            val result = String(bytes, 0, len)
            if (result.matches(checker)) Result.Success(result)
            else Result.Error(SocketSyntaxException())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}