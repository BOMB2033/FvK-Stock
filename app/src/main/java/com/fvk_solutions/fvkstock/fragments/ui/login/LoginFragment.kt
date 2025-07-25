package com.fvk_solutions.fvkstock.fragments.ui.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.util.Log
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.fvk_solutions.fvkstock.R
import com.fvk_solutions.fvkstock.databinding.FragmentLoginBinding
import com.fvk_solutions.fvkstock.scanLocalNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LoginFragment", "onCreateView: Создание представления фрагмента входа")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
        Log.d("LoginFragment", "onViewCreated: Представление фрагмента входа создано и ViewModel инициализирован")

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        GlobalScope.launch(Dispatchers.IO) {
            Log.d("LoginFragment", "onViewCreated: Запуск функции сканирования сети")
            val foundIps = scanLocalNetwork(requireContext())
            Log.d("LoginFragment", "onViewCreated: Найденные IP-адреса: $foundIps")
        }

        loginViewModel.loginFormState.observe(
            viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    Log.d("LoginFragment", "loginFormState.observe: Состояние формы входа null, возврат")
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
                Log.d("LoginFragment", "loginFormState.observe: Состояние формы входа обновлено, isDataValid: ${loginFormState.isDataValid}")
            })

        loginViewModel.loginResult.observe(
            viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: run {
                    Log.d("LoginFragment", "loginResult.observe: Результат входа null, возврат")
                    return@Observer
                }
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                    Log.e("LoginFragment", "loginResult.observe: Ошибка входа: ${getString(it)}")
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                    Log.i("LoginFragment", "loginResult.observe: Вход успешен для пользователя: ${it.displayName}")
                }
            })
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()

                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    // todo: добавить логирование
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            Log.d("LoginFragment", "loginButton.setOnClickListener: Кнопка входа нажата, попытка входа")
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Log.i("LoginFragment", "updateUiWithUser: Обновление UI для пользователя: ${model.displayName}")
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Log.w("LoginFragment", "showLoginFailed: Отображение сообщения об ошибке входа: ${getString(errorString)}")
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LoginFragment", "onDestroyView: Представление фрагмента входа уничтожается")
        _binding = null
    }
}