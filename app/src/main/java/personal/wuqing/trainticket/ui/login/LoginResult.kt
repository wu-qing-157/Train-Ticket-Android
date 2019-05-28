package personal.wuqing.trainticket.ui.login

import java.io.Serializable

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
) : Serializable
