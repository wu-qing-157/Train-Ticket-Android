package personal.wuqing.trainticket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.buy_ticket.*
import personal.wuqing.trainticket.data.PriceAndNum
import personal.wuqing.trainticket.data.SingleTicket
import personal.wuqing.trainticket.ui.afterTextChanged
import java.text.DecimalFormat

class BuyTicketActivity : AppCompatActivity() {
    companion object {
        const val SINGLE_TICKET = "s_t"
        const val PRICE_AND_NUM = "pan"
    }

    lateinit var trainId: String
    var max = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buy_ticket)

        (intent.extras!![SINGLE_TICKET] as SingleTicket).apply {
            this@BuyTicketActivity.trainId = trainId
            train_name.text = trainName
            depart_station.text = departStation
            depart_time.text = getString(R.string.date_and_time, departDate, departTime)
            arrive_station.text = arriveStation
            arrive_time.text = getString(R.string.date_and_time, arriveDate, arriveTime)
        }
        (intent.extras!![PRICE_AND_NUM] as PriceAndNum).apply {
            kind_name.text = name
            max = num
            num_left.text = getString(R.string.display_num, num)
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
            if (it.isNotEmpty()) {
                if (it.toInt() < 1) buy_num.editText!!.setText("1")
                if (it.toInt() > max) buy_num.editText!!.setText(max.toString())
            }
        }
    }
}
