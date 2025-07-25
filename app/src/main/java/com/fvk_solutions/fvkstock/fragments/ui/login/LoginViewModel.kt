package com.fvk_solutions.fvkstock.fragments.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import android.util.Patterns
import com.fvk_solutions.fvkstock.fragments.data.LoginRepository
import com.fvk_solutions.fvkstock.fragments.data.Result

import com.fvk_solutions.fvkstock.R
import java.util.concurrent.Executors

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult


    fun login(username: String, password: String) {
        Log.i("LoginViewModel", "Попытка входа пользователя: $username")
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            Log.i("LoginViewModel", "Пользователь $username успешно вошел в систему")
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.user.displayName))
        } else {
            Log.w("LoginViewModel", "Ошибка входа для пользователя: $username")
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
            Log.d("LoginViewModel", "Неверное имя пользователя: $username")
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
            Log.d("LoginViewModel", "Неверный пароль")
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
            Log.d("LoginViewModel", "Данные для входа валидны")
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

}

