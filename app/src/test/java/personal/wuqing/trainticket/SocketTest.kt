package personal.wuqing.trainticket

import org.junit.Test
import personal.wuqing.trainticket.network.ResultRegex
import personal.wuqing.trainticket.network.SocketWork

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SocketTest {
    @Test
    fun login() {
        println(SocketWork.getResult("login 12345 abcdef", ResultRegex.LOGIN))
    }
}
