package personal.wuqing.trainticket

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import personal.wuqing.trainticket.data.LoginFailException
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.data.getDisplayName
import personal.wuqing.trainticket.data.login
import personal.wuqing.trainticket.network.SocketSyntaxException
import personal.wuqing.trainticket.ui.alert
import java.io.Serializable
import java.net.ConnectException
import java.net.SocketTimeoutException

class LoginActivity : AppCompatActivity() {

    companion object {
        const val LOGIN_RESULT = "log_res"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Login()
    }

    inner class Login {
        init {
            setContentView(R.layout.activity_login)
            login_user_id.editText!!.afterTextChanged { reportValidity() }
            login_password.editText!!.apply {
                afterTextChanged { reportValidity() }
                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> if (login.isEnabled) login.performClick()
                    }
                    false
                }
            }
            login.setOnClickListener {
                login_loading.visibility = View.VISIBLE
                listOf(login_user_id, login_password, login, jump_register).forEach { it.isEnabled = false }
                Thread {
                    when (val result = login(
                        login_user_id.editText!!.text.toString(),
                        login_password.editText!!.text.toString()
                    )) {
                        is Result.Success -> {
                            val userId = result.data
                            when (val nameResult = getDisplayName(userId)) {
                                is Result.Success -> {
                                    setResult(MainActivity.LOGIN_REQUEST, Intent().putExtra(LOGIN_RESULT, LoginResult(userId, nameResult.data)))
                                    finish()
                                }
                                is Result.Error -> login.post {
                                    alert {
                                        setTitle(R.string.login_failed)
                                        setMessage(
                                            when (nameResult.exception) {
                                                is ConnectException -> getString(R.string.failed_connection_refused)
                                                is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                                                is SocketSyntaxException -> getString(R.string.failed_bad_return)
                                                else -> getString(R.string.failed_unknown).format(nameResult.exception)
                                            }
                                        )
                                        setPositiveButton(R.string.action_accept_bad, null)
                                    }
                                    login_loading.visibility = View.INVISIBLE
                                    listOf(login_user_id, login_password, login, jump_register).forEach { it.isEnabled = true }
                                }
                            }
                        }
                        is Result.Error -> login.post {
                            alert {
                                setTitle(R.string.login_failed)
                                setMessage(
                                    when (result.exception) {
                                        is ConnectException -> getString(R.string.failed_connection_refused)
                                        is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                                        is SocketSyntaxException -> getString(R.string.failed_bad_return)
                                        is LoginFailException -> getString(R.string.failed_password_mismatch)
                                        else -> getString(R.string.failed_unknown).format(result.exception)
                                    }
                                )
                                setPositiveButton(R.string.action_accept_bad, null)
                            }
                            login_loading.visibility = View.INVISIBLE
                            listOf(login_user_id, login_password, login, jump_register).forEach { it.isEnabled = true }
                        }
                    }
                }.start()
            }
            jump_register.setOnClickListener {
                Register()
            }
        }

        private fun reportValidity() {
            val userIdValidity = login_user_id.editText!!.text.matches(Regex("""[0-9]{4,}"""))
            val passwordValidity = login_password.editText!!.text.matches(Regex("""\S{6,}"""))
            login_user_id.error = if (userIdValidity) null else getString(R.string.invalid_user_id)
            login_password.error = if (passwordValidity) null else getString(R.string.invalid_password_login)
            login.isEnabled = userIdValidity && passwordValidity
        }
    }

    inner class Register {
        init {
            setContentView(R.layout.activity_register)
            register_password.editText!!.afterTextChanged { reportValidity() }
            name.editText!!.afterTextChanged { reportValidity() }
            email.editText!!.afterTextChanged { reportValidity() }
            phone.editText!!.apply {
                afterTextChanged { reportValidity() }
                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> if (register.isEnabled) register.performClick()
                    }
                    false
                }
            }
            register.setOnClickListener {
                register_loading.visibility = View.VISIBLE
                listOf(
                    register_user_id,
                    register_password,
                    name,
                    email,
                    phone,
                    register,
                    jump_login
                ).forEach { it.isEnabled = false }
            }
            jump_login.setOnClickListener {
                Login()
            }
        }

        private fun reportValidity() {
            val passwordValidity = register_password.editText!!.text.matches(Regex("""\S{6,}"""))
            val nameValidity = name.editText!!.text.matches(Regex("""\S{1,10}"""))
            val emailValidity =
                email.editText!!.text.matches(Regex("""(?!\S{21})\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*"""))
            val phoneValidity = phone.editText!!.text.matches(Regex("""(?!\S{21})\+?[0-9]{3,}"""))
            register_password.error = if (passwordValidity) null else getString(R.string.invalid_password_register)
            name.error = if (nameValidity) null else getString(R.string.invalid_name)
            email.error = if (emailValidity) null else getString(R.string.invalid_email)
            phone.error = if (phoneValidity) null else getString(R.string.invalid_phone)
            register.isEnabled = passwordValidity && nameValidity && emailValidity && phoneValidity
        }
    }
}

data class LoginResult(val userId: String, val username: String) : Serializable

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
