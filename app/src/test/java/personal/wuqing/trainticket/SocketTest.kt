package personal.wuqing.trainticket

import org.junit.Test
import personal.wuqing.trainticket.data.PriceAndNum
import personal.wuqing.trainticket.data.SingleTicket
import personal.wuqing.trainticket.network.ResultRegex
import personal.wuqing.trainticket.network.SocketWork
import java.io.ObjectOutputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SocketTest {
    @Test
    fun login() {
        println(personal.wuqing.trainticket.network.login("1234", "abcdef"))
    }

    @Test
    fun write() {
        val output = ObjectOutputStream(System.out)
        output.writeObject(SingleTicket("", "", "", "", "", "", "", "", listOf(PriceAndNum("", 1.2, 3))))
    }
}
