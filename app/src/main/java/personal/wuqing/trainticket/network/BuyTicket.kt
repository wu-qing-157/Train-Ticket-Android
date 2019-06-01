package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result

fun buyTicket(
    userId: String, num: Int, trainId: String,
    depart: String, arrive: String, date: String, kind: String
): Result<Unit> =
    when (val result = SocketWork.getResult(
        "buy_ticket $userId $num $trainId $depart $arrive $date $kind"
    )) {
        is Result.Success -> when (result.data) {
            "0" -> Result.Error(BuyTicketFailException())
            "1" -> Result.Success(Unit)
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class BuyTicketFailException : Exception()