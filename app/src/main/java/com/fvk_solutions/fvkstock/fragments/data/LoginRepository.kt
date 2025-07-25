package com.fvk_solutions.fvkstock.fragments.data

import android.util.Log
import com.fvk_solutions.fvkstock.RetrofitInstance
import com.fvk_solutions.fvkstock.models.LoginApiResponse
import com.fvk_solutions.fvkstock.models.LoginRequest
import com.fvk_solutions.fvkstock.models.UserApiResponse
import retrofit2.Response
import java.io.IOException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: UserApiResponse? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        Log.i("LoginRepository", "Выход из системы пользователя")
        user = null
        dataSource.logout()
    }

    private val apiService = RetrofitInstance.api

    private val TAG = "LoginRepository"

    fun login(username: String, password: String): Result<LoginApiResponse> {
        Log.i(TAG, "Попытка входа пользователя: $username")
        return try {
            val result = dataSource.login(username, password)
            if (result is Result.Success) {
                Log.i(TAG, "Вход успешен для пользователя: $username")
                setLoggedInUser(result.data.user)
            } else if (result is Result.Error) {
                Log.e(TAG, "Ошибка входа: ${result.exception.message}")
            }
            result
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при входе в систему для пользователя: $username", e)
            Result.Error(e)
        }
    }

    private fun setLoggedInUser(user: Any?) {
        // Реализация сохранения пользователя, например, в SharedPreferences
        // Пример: userTokenManager.saveUser(user)
        Log.d(TAG, "Сохранение пользователя: $user")
    }
}