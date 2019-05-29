package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result

fun login(userId: String, password: String): Result<String> =
    when (val result = SocketWork.getResult("login $userId $password")) {
        is Result.Success -> when (result.data) {
            "0" -> Result.Error(LoginFailException())
            "1" -> Result.Success(userId)
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class LoginFailException : Exception()