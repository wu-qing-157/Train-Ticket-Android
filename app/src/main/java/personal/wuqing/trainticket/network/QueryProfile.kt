package personal.wuqing.trainticket.network

import personal.wuqing.trainticket.data.Profile
import personal.wuqing.trainticket.data.Result

fun queryProfile(userId: String): Result<Profile> = when (val result = SocketWork.getResult("query_profile $userId")) {
    is Result.Success -> if (result.data.matches(ResultRegex.QUERY_PROFILE)) {
        val (name, email, phone, administrator) = result.data.split(" ")
        Result.Success(
            Profile(
                name,
                email,
                phone,
                administrator == "2"
            )
        )
    } else {
        Result.Error(SocketSyntaxException())
    }
    is Result.Error -> Result.Error(result.exception)
}

fun getDisplayName(userId: String) = when (val result = queryProfile(userId)) {
    is Result.Success -> Result.Success(result.data.name)
    is Result.Error -> Result.Error<String>(result.exception)
}
