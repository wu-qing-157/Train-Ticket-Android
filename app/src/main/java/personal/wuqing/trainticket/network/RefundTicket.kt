package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result

fun refundTicket(
    userId: String, num: Int, trainId: String, depart: String, arrive: String,
    date: String, kind: String
): Result<Unit> =
    when (val result = SocketWork.getResult("refund_ticket $userId $num $trainId $depart $arrive $date $kind")) {
        is Result.Success -> when (result.data) {
            "1" -> Result.Success(Unit)
            "0" -> Result.Error(RefundFailedException())
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class RefundFailedException : Exception()