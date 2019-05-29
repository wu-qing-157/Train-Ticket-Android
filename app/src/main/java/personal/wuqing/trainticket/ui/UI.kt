package personal.wuqing.trainticket.ui

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Context

fun Context.alert(build: MaterialAlertDialogBuilder.() -> Unit) {
    val builder = MaterialAlertDialogBuilder(this)
    builder.build()
    builder.show()
}