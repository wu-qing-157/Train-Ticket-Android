package personal.wuqing.trainticket.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

fun Context.alert(build: MaterialAlertDialogBuilder.() -> Unit): MaterialAlertDialogBuilder {
    val builder = MaterialAlertDialogBuilder(this)
    builder.build()
    builder.show()
    return builder
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun TextInputLayout.reportValidity(checker: Regex, @StringRes info: Int, disableError: Boolean = false): Boolean {
    val valid = editText!!.text.toString().matches(checker)
    if (valid || editText!!.text.isEmpty()) {
        error = null
        if (disableError) isErrorEnabled = false
    } else {
        error = editText!!.context.getString(info)
    }
    return valid
}