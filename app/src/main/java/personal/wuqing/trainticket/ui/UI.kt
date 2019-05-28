package personal.wuqing.trainticket.ui

import android.app.AlertDialog
import android.content.Context

fun Context.alert(build: AlertDialog.Builder.() -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.build()
    builder.show()
}