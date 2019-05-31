package personal.wuqing.trainticket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.network.*
import personal.wuqing.trainticket.ui.afterTextChanged
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
        setContentView(R.layout.activity_login)
        login_user_id.editText!!.afterTextChanged { reportLoginValidity() }
        login_password.editText!!.apply {
            afterTextChanged { reportLoginValidity() }
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
            loginAsync()
        }
        jump_register.setOnClickListener {
            view_animator.showNext()
        }
        register_password.editText!!.afterTextChanged { reportRegisterValidity() }
        name.editText!!.afterTextChanged { reportRegisterValidity() }
        email.editText!!.afterTextChanged { reportRegisterValidity() }
        phone.editText!!.apply {
            afterTextChanged { reportRegisterValidity() }
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
            registerAsync()
        }
        jump_login.setOnClickListener {
            view_animator.showPrevious()
        }
    }

    private fun loginAsync() {
        Thread {
            when (val result = login(
                login_user_id.editText!!.text.toString(),
                login_password.editText!!.text.toString()
            )) {
                is Result.Success -> {
                    val userId = result.data
                    when (val nameResult = getDisplayName(userId)) {
                        is Result.Success -> {
                            setResult(
                                MainActivity.LOGIN_REQUEST,
                                Intent().putExtra(LOGIN_RESULT, LoginResult(userId, nameResult.data))
                            )
                            finish()
                        }
                        is Result.Error -> login.post { loginFail(nameResult.exception) }
                    }
                }
                is Result.Error -> login.post { loginFail(result.exception) }
            }
        }.start()
    }

    private fun loginFail(exception: Exception) {
        alert {
            setTitle(R.string.login_failed)
            setMessage(
                when (exception) {
                    is ConnectException -> getString(R.string.failed_connection_refused)
                    is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                    is SocketSyntaxException -> getString(R.string.failed_bad_return)
                    is LoginFailException -> getString(R.string.failed_password_mismatch)
                    else -> getString(R.string.failed_unknown, exception)
                }
            )
            setPositiveButton(R.string.action_accept_bad, null)
        }
        login_loading.visibility = View.INVISIBLE
        listOf(login_user_id, login_password, login, jump_register).forEach { it.isEnabled = true }
    }

    private fun registerAsync() {
        Thread {
            when (val result = register(
                register_password.editText!!.text.toString(),
                name.editText!!.text.toString(),
                email.editText!!.text.toString(),
                phone.editText!!.text.toString()
            )) {
                is Result.Success -> {
                    val userId = result.data
                    when (val nameResult = getDisplayName(userId)) {
                        is Result.Success -> register.post {
                            alert {
                                setTitle(R.string.register_success)
                                setMessage(getString(R.string.register_success_info, userId))
                                setPositiveButton(R.string.action_accept_good) { _, _ ->
                                    setResult(
                                        MainActivity.LOGIN_REQUEST,
                                        Intent().putExtra(LOGIN_RESULT, LoginResult(userId, nameResult.data))
                                    )
                                    finish()
                                }
                                setCancelable(false)
                            }
                        }
                        is Result.Error -> register.post { registerFail(nameResult.exception) }
                    }
                }
                is Result.Error -> register.post { registerFail(result.exception) }
            }
        }.start()
    }

    private fun registerFail(exception: Exception) {
        alert {
            setTitle(R.string.register_failed)
            setMessage(
                when (exception) {
                    is ConnectException -> getString(R.string.failed_connection_refused)
                    is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                    is SocketSyntaxException -> getString(R.string.failed_bad_return)
                    is RegisterFailException -> getString(R.string.failed_register_rejected)
                    else -> getString(R.string.failed_unknown, exception)
                }
            )
            setPositiveButton(R.string.action_accept_bad, null)
        }
        register_loading.visibility = View.INVISIBLE
        listOf(
            register_user_id,
            register_password,
            name,
            email,
            phone,
            register,
            jump_login
        ).forEach { it.isEnabled = true }
    }

    private fun reportLoginValidity() {
        val userIdValidity = login_user_id.editText!!.text.matches(Regex("""[0-9]{4,10}"""))
        val passwordValidity = login_password.editText!!.text.matches(Regex("""\S{6,20}"""))
        login_user_id.error =
            if (userIdValidity || login_user_id.editText!!.text.isEmpty()) null else getString(R.string.invalid_user_id)
        login_password.error =
            if (passwordValidity || login_password.editText!!.text.isEmpty()) null else getString(R.string.invalid_password_login)
        login.isEnabled = userIdValidity && passwordValidity
    }

    private fun reportRegisterValidity() {
        val passwordValidity = register_password.editText!!.text.matches(Regex("""\S{6,}"""))
        val nameValidity = name.editText!!.text.matches(Regex("""\S{1,10}"""))
        val emailValidity =
            email.editText!!.text.matches(Regex("""(?!\S{21})\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*"""))
        val phoneValidity = phone.editText!!.text.matches(Regex("""(?!\S{21})\+?[0-9]{3,}"""))
        register_password.error =
            if (passwordValidity || register_password.editText!!.text.isEmpty()) null else getString(R.string.invalid_password_register)
        name.error =
            if (nameValidity || name.editText!!.text.isEmpty()) null else getString(R.string.invalid_name)
        email.error =
            if (emailValidity || email.editText!!.text.isEmpty()) null else getString(R.string.invalid_email)
        phone.error =
            if (phoneValidity || phone.editText!!.text.isEmpty()) null else getString(R.string.invalid_phone)
        register.isEnabled = passwordValidity && nameValidity && emailValidity && phoneValidity
    }
}

data class LoginResult(val userId: String, val username: String) : Serializable
