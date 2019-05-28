package personal.wuqing.trainticket.data

import personal.wuqing.trainticket.data.model.LoggedInUser
import personal.wuqing.trainticket.network.ResultRegex
import personal.wuqing.trainticket.network.SocketUnknownException
import personal.wuqing.trainticket.network.SocketWork
import personal.wuqing.trainticket.ui.login.LoginFailException
import java.io.IOException

class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            when (val loginResult = SocketWork.getResult("login $username $password", ResultRegex.LOGIN)) {
                is Result.Success -> when (loginResult.data) {
                    "1" -> {
                        when (val queryProfileResult = SocketWork.getResult("query_profile $username", ResultRegex.QUERY_PROFILE)) {
                            is Result.Success -> Result.Success(LoggedInUser(username, queryProfileResult.data.split(" ")[1]))
                            is Result.Error -> Result.Error(queryProfileResult.exception)
                        }
                    }
                    "0" -> Result.Error(LoginFailException())
                    else -> Result.Error(SocketUnknownException())
                }
                is Result.Error -> Result.Error(loginResult.exception)
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}

