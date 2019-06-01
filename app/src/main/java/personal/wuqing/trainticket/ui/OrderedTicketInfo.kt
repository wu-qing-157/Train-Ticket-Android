package personal.wuqing.trainticket.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ordered_info.view.*
import personal.wuqing.trainticket.MainActivity
import personal.wuqing.trainticket.R
import personal.wuqing.trainticket.data.OrderedTicket
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.network.RefundFailedException
import personal.wuqing.trainticket.network.RegisterFailException
import personal.wuqing.trainticket.network.SocketSyntaxException
import personal.wuqing.trainticket.network.refundTicket
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

fun MainActivity.inflateOrderedTicket(info: OrderedTicket, root: ViewGroup, disableRefund: Boolean = false) {
    val layout = LinearLayout(this)
    layoutInflater.inflate(R.layout.ordered_info, layout)
    layout.train_name.text = info.trainName
    layout.depart_station.text = info.departStation
    layout.arrive_station.text = info.arriveStation
    layout.depart_time.text = getString(R.string.date_and_time, info.departDate, info.departTime)
    layout.arrive_time.text = getString(R.string.date_and_time, info.arriveDate, info.arriveTime)
    layout.kind_name.text = info.kindName
    layout.ordered_num.text = getString(R.string.display_ordered_num, info.num)
    if (disableRefund)
        layout.refund_button.visibility = View.GONE
    else
        layout.refund_button.setOnClickListener {
            alert {
                setTitle(R.string.confirm_refund)
                val dialogLayout = LinearLayout(this@inflateOrderedTicket)
                inflateOrderedTicket(info, dialogLayout, true)
                setView(dialogLayout)
                setPositiveButton(R.string.action_ok) { _, _ ->
                    Thread {
                        when (val result = refundTicket(
                            userId = userId, trainId = info.trainId, num = info.num, kind = info.kindName,
                            depart = info.departStation, arrive = info.arriveStation, date = info.departDate
                        )) {
                            is Result.Success -> runOnUiThread {
                                alert {
                                    setTitle(R.string.refund_success)
                                    val successLayout = LinearLayout(this@inflateOrderedTicket)
                                    inflateOrderedTicket(info, successLayout, true)
                                    setView(successLayout)
                                    setPositiveButton(R.string.action_accept_good, null)
                                }
                                updateOrderedTicket()
                            }
                            is Result.Error -> runOnUiThread {
                                alert {
                                    setTitle(R.string.refund_failed)
                                    setMessage(
                                        when (result.exception) {
                                            is ConnectException, is SocketException -> getString(R.string.failed_connection_refused)
                                            is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                                            is SocketSyntaxException -> getString(R.string.failed_bad_return)
                                            is RefundFailedException -> getString(R.string.failed_refund_rejected)
                                            else -> getString(R.string.failed_unknown, result.exception)
                                        }
                                    )
                                    setPositiveButton(R.string.action_accept_bad, null)
                                }
                                updateOrderedTicket()
                            }
                        }
                    }.start()
                }
                setNegativeButton(R.string.action_cancel, null)
            }
        }
    root.addView(layout)
}