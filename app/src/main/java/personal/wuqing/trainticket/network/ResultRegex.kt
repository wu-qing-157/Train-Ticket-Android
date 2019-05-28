package personal.wuqing.trainticket.network

object ResultRegex {
    val LOGIN = Regex("""[01]""")
    val QUERY_PROFILE = Regex("""\S{1,10} (?!\S{21})\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)* (?!\S{21})\+?[0-9]{3,} [12]""")
}