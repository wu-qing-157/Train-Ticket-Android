package personal.wuqing.trainticket.data

import java.io.Serializable

data class SingleTicket(
    val trainId: String, val trainName: String, val departStation: String, val arriveStation: String,
    val departDate: String, val arriveDate: String,
    val departTime: String, val arriveTime: String, val tickets: List<PriceAndNum>
) : Serializable