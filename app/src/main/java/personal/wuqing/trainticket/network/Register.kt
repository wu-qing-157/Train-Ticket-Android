package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Result

fun register(password: String, name: String, email: String, phone: String): Result<String> =
    when (val result = SocketWork.getResult("register $password $name $email $phone")) {
        is Result.Success -> when {
            result.data == "0" -> Result.Error(RegisterFailException())
            result.data.matches(Regex("""\d{4,10}""")) -> Result.Success(result.data)
            else -> Result.Error(SocketSyntaxException())
        }
        is Result.Error -> Result.Error(result.exception)
    }

class RegisterFailException : Exception()