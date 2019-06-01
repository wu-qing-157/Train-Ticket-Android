package personal.wuqing.trainticket.data

data class OrderedTicket(
    val trainId: String, val trainName: String,
    val departStation: String, val departDate: String, val departTime: String,
    val arriveStation: String, val arriveDate: String, val arriveTime: String,
    val kindName: String, val num: Int
)