package personal.wuqing.trainticket.network

object ResultRegex {
    val QUERY_PROFILE = Regex("""\S{1,10} (?!\S{21})\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)* (?!\S{21})\+?[0-9]{3,} [12]""")
    val QUERY_TICKET =
        Regex("""([a-zA-Z0-9]{1,10} {3}\S{1,10} {3}[A-Z]( {3}\S{1,10} {3}\d{4}-\d{2}-\d{2} {3}((\d{2}:\d{2})|(xx:xx))){2} {3}(\S{1,10} \d+ \S+)( {2}\S{1,10} \d+ \S+)*( {4}[a-zA-Z0-9]{1,10} {3}\S{1,10} {3}[A-Z]( {3}\S{1,10} {3}\d{4}-\d{2}-\d{2} {3}((\d{2}:\d{2})|(xx:xx))){2} {3}(\S{1,10} \d+ \S+)( {2}\S{1,10} \d+ \S+)*)*)""")
}