package personal.wuqing.trainticket.ui

import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ordered_info.view.*
import personal.wuqing.trainticket.MainActivity
import personal.wuqing.trainticket.R
import personal.wuqing.trainticket.data.OrderedTicket

fun MainActivity.inflateOrderedTicket(info: OrderedTicket, root: ViewGroup) {
    val layout = LinearLayout(this)
    layoutInflater.inflate(R.layout.ordered_info, layout)
    layout.train_name.text = info.trainName
    layout.depart_station.text = info.departStation
    layout.arrive_station.text = info.arriveStation
    layout.depart_time.text = getString(R.string.date_and_time, info.departDate, info.departTime)
    layout.arrive_time.text = getString(R.string.date_and_time, info.arriveDate, info.arriveTime)
    layout.kind_name.text = info.kindName
    layout.ordered_num.text = info.num.toString()
    layout.refund_button.setOnClickListener {
        alert {
            setTitle(R.string.confirm_refund)
            val dialogLayout = LinearLayout(this@inflateOrderedTicket)
            inflateOrderedTicket(info, dialogLayout)
            setView(dialogLayout)
            setPositiveButton(R.string.action_ok) { _, _ ->
            }
            setNegativeButton(R.string.action_cancel, null)
        }
    }
    root.addView(layout)
}