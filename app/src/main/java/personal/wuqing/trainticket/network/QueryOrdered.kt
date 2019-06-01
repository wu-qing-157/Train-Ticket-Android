package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.OrderedTicket
import personal.wuqing.trainticket.data.Result

fun queryOrdered(userId: String): Result<List<OrderedTicket>> =
    when (val result = SocketWork.getResult("query_ordered $userId")) {
        is Result.Success -> when {
            result.data == "-1" -> Result.Error(QueryOrderedEmptyException())
            result.data.matches(ResultRegex.QUERY_ORDERED) ->
                Result.Success(result.data.split(Regex(""" {2}""")).map {
                    val info = it.split(Regex(""" """))
                    OrderedTicket(
                        info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7], info[8], info[9].toInt()
                    )
                })
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class QueryOrderedEmptyException : Exception()