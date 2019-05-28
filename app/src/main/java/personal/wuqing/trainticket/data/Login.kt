package personal.wuqing.trainticket.data

import personal.wuqing.trainticket.network.SocketWork

fun login(userId: String, password: String): Result<String> =
    when (val result = SocketWork.getResult("login $userId $password")) {
        is Result.Success -> when (result.data) {
            "0" -> Result.Error(LoginFailException())
            "1" -> Result.Success(userId)
            else -> Result.Error(DataUnknownException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class LoginFailException : Exception()