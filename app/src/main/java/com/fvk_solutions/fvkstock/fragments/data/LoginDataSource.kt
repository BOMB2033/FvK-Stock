package com.fvk_solutions.fvkstock.fragments.data

import android.util.Log
import com.fvk_solutions.fvkstock.RetrofitInstance
import com.fvk_solutions.fvkstock.models.LoginApiResponse
import com.fvk_solutions.fvkstock.models.LoginRequest
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class LoginDataSource {

    private val apiService = RetrofitInstance.api
    private val TAG = "LoginDataSource"
    private val executor = Executors.newSingleThreadExecutor()

    fun login(username: String, password: String): Result<LoginApiResponse> {
        Log.d(TAG, "Попытка входа пользователя: $username")
        return try {
            val loginRequest = LoginRequest(username, password)
            // Выполняем сетевой запрос в фоновом потоке
            val future = CompletableFuture.supplyAsync({
                try {
                    val response: Response<LoginApiResponse> = apiService.login(loginRequest).execute()
                    Log.d(TAG, "Получен ответ от сервера: ${response.code()}")
                    if (response.isSuccessful) {
                        val loggedInUser = response.body()
                        if (loggedInUser != null) {
                            Log.i(TAG, "Вход успешен для пользователя: $username")
                            Result.Success(loggedInUser)
                        } else {
                            Log.e(TAG, "Ошибка входа: Тело ответа пустое. Код: ${response.code()}")
                            Result.Error(IOException("Ошибка входа: Тело ответа пустое. Код: ${response.code()}"))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody.isNullOrEmpty()) {
                            "Ошибка входа: ${response.code()} ${response.message()}"
                        } else {
                            "Ошибка входа: ${response.code()} - $errorBody"
                        }
                        Log.e(TAG, "Ошибка входа: $errorMessage")
                        Result.Error(IOException(errorMessage))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при входе в систему для пользователя: $username", e)
                    Result.Error(IOException("Ошибка при входе в систему: ${e.message}", e))
                }
            }, executor)

            // Ожидаем результат из фонового потока
            future.get() // Блокирует до завершения, но в фоновом потоке
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при входе в систему для пользователя: $username", e)
            Result.Error(IOException("Ошибка при входе в систему: ${e.message}", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
        executor.shutdown() // Очищаем executor при выходе
    }
}