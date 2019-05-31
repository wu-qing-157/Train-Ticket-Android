package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.PriceAndNum
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.data.SingleTicket

fun queryTicket(
    depart: CharSequence, arrive: CharSequence, date: CharSequence,
    catalog: Iterable<Char>
): Result<List<SingleTicket>> =
    when (val result = SocketWork.getResult("query_ticket $depart $arrive $date ${catalog.joinToString("")}")) {
        is Result.Success -> when {
            result.data == "-1" -> Result.Error(QuertTicketNoMatchException())
            result.data.matches(ResultRegex.QUERY_TICKET) ->
                Result.Success(result.data.split(Regex(""" {4}""")).map {
                    val info = it.split(Regex(""" {3}"""))
                    SingleTicket(trainId = info[0], trainName = info[1], departStation = info[3],
                        departDate = info[4], departTime = info[5], arriveStation = info[6], arriveDate = info[7],
                        arriveTime = info[8], tickets = info[9].split(Regex(""" {2}""")).map { kindInfo ->
                            kindInfo.split(Regex(""" """))
                                .let { block ->
                                    PriceAndNum(name = block[0], num = block[1].toInt(), price = block[2].toDouble())
                                }
                        })
                })
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class QuertTicketNoMatchException : Exception()