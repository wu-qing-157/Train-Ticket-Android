package personal.wuqing.trainticket.ui

import android.content.Intent
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.price_and_num.view.*
import kotlinx.android.synthetic.main.ticket_info.view.*
import personal.wuqing.trainticket.BuyTicketActivity
import personal.wuqing.trainticket.MainActivity
import personal.wuqing.trainticket.R
import personal.wuqing.trainticket.data.SingleTicket
import java.text.DecimalFormat

fun MainActivity.inflateTicketInfoCard(info: SingleTicket, root: ViewGroup) {
    val layout = LinearLayout(this)
    layoutInflater.inflate(R.layout.ticket_info, layout)
    layout.train_name.text = info.trainName
    layout.depart_station.text = info.departStation
    layout.arrive_station.text = info.arriveStation
    layout.depart_time.text = getString(R.string.date_and_time, info.departDate, info.departTime)
    layout.arrive_time.text = getString(R.string.date_and_time, info.arriveDate, info.arriveTime)
    for (item in info.tickets) {
        val innerLayout = LinearLayout(this)
        layoutInflater.inflate(R.layout.price_and_num, innerLayout)
        innerLayout.kind_name.text = item.name
        innerLayout.price_single.text = getString(R.string.display_price, DecimalFormat("#.##").format(item.price))
        innerLayout.num_left.text = getString(R.string.display_num, item.num)
        innerLayout.materialButton.setOnClickListener {
            if (userId == "") {
                launchLogin()
            } else {
                startActivityForResult(
                    Intent(this, BuyTicketActivity::class.java)
                        .putExtra(BuyTicketActivity.SINGLE_TICKET, info)
                        .putExtra(BuyTicketActivity.PRICE_AND_NUM, item)
                        .putExtra(BuyTicketActivity.USER_ID, userId),
                    MainActivity.REFUND_TICKET_REQUEST
                )
            }
        }
        layout.ticket_kinds.addView(
            innerLayout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f
            )
        )
    }
    root.addView(layout)
}
