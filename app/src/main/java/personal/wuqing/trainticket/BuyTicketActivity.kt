package personal.wuqing.trainticket

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.buy_ticket.*
import personal.wuqing.trainticket.data.PriceAndNum
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.data.SingleTicket
import personal.wuqing.trainticket.network.BuyTicketFailException
import personal.wuqing.trainticket.network.SocketSyntaxException
import personal.wuqing.trainticket.network.buyTicket
import personal.wuqing.trainticket.ui.afterTextChanged
import personal.wuqing.trainticket.ui.alert
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.DecimalFormat

class BuyTicketActivity : AppCompatActivity() {
    companion object {
        const val SINGLE_TICKET = "s_t"
        const val PRICE_AND_NUM = "pan"
        const val USER_ID = "ui"
    }

    lateinit var trainId: String
    lateinit var userId: String
    lateinit var departStation: String
    lateinit var arriveStation: String
    lateinit var ticketKind: String
    lateinit var departDate: String
    var singlePrice = 0.0
    var numLimit = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buy_ticket)

        userId = intent.extras!![USER_ID] as String
        (intent.extras!![SINGLE_TICKET] as SingleTicket).apply {
            this@BuyTicketActivity.trainId = trainId
            train_name.text = trainName
            depart_station.text = departStation
            depart_time.text = getString(R.string.date_and_time, departDate, departTime)
            arrive_station.text = arriveStation
            arrive_time.text = getString(R.string.date_and_time, arriveDate, arriveTime)
            this@BuyTicketActivity.departStation = departStation
            this@BuyTicketActivity.arriveStation = arriveStation
            this@BuyTicketActivity.departDate = departDate
        }
        (intent.extras!![PRICE_AND_NUM] as PriceAndNum).apply {
            ticketKind = name
            kind_name.text = name
            numLimit = num
            num_left.text = getString(R.string.display_num, num)
            singlePrice = price
            price_single.text = getString(R.string.display_price, DecimalFormat("#.##").format(price))
            price_info.text = getString(R.string.display_price, DecimalFormat("#.##").format(price))
        }
        minus.setOnClickListener {
            buy_num.editText!!.apply {
                setText((text.toString().toInt() - 1).toString())
            }
        }
        add.setOnClickListener {
            buy_num.editText!!.apply {
                setText((text.toString().toInt() + 1).toString())
            }
        }
        buy_num.editText!!.afterTextChanged {
            if (it.isEmpty()) {
                price_info.text = getString(R.string.invalid_num)
            } else {
                if (it.toInt() < 1) buy_num.editText!!.setText("1")
                if (it.toInt() > numLimit) buy_num.editText!!.setText(numLimit.toString())
                price_info.text =
                    getString(R.string.display_price, DecimalFormat("#.##").format(it.toInt() * singlePrice))
            }
        }
        buy.setOnClickListener {
            listOf(minus, add, buy_num, buy).forEach { it.isEnabled = false }
            buy_loading.visibility = View.VISIBLE
            buyTicketAsync()
        }
    }

    private fun buyTicketAsync() = Thread {
        when (val result = buyTicket(
            userId, buy_num.editText!!.text.toString().toInt(), trainId, departStation, arriveStation,
            departDate, ticketKind
        )) {
            is Result.Success -> runOnUiThread {
                alert {
                    setTitle(R.string.buy_ticket_success)
                    setPositiveButton(R.string.action_accept_good) { _, _ -> finish() }
                    setCancelable(false)
                }
            }
            is Result.Error -> runOnUiThread {
                alert {
                    setTitle(R.string.buy_ticket_failed)
                    setMessage(
                        when (result.exception) {
                            is ConnectException, is SocketException -> getString(R.string.failed_connection_refused)
                            is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                            is SocketSyntaxException -> getString(R.string.failed_bad_return)
                            is BuyTicketFailException -> getString(R.string.failed_buy_ticket_rejected)
                            else -> getString(R.string.failed_unknown, result.exception)
                        }
                    )
                    setPositiveButton(R.string.action_accept_bad, null)
                }
                buy_loading.visibility = View.INVISIBLE
                listOf(minus, add, buy_num, buy).forEach { it.isEnabled = true }
            }
        }
    }.start()
}
