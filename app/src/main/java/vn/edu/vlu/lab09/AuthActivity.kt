package vn.edu.vlu.lab09

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import vn.edu.vlu.lab09.databinding.ActivityAuthBinding

/**
 * AuthActivity: màn hình Đăng ký / Đăng nhập bằng Email + Password qua Firebase Authentication.
 * - Dùng View Binding thay cho findViewById().
 * - Tự động đăng nhập nếu phiên trước vẫn còn hiệu lực (currentUser != null).
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    // FirebaseAuth (singleton) quản lý phiên đăng nhập
    private lateinit var auth: FirebaseAuth

    // true = chế độ Đăng ký, false = chế độ Đăng nhập
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()

        auth = FirebaseAuth.getInstance()

        // (1) Auto-login: đã có phiên đăng nhập thì vào thẳng MainActivity
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupModeToggle()
        setupSubmitButton()
        setupForgotPassword()
    }

    /**
     * targetSdk 35+ bắt buộc edge-to-edge: thêm padding bằng kích thước thanh hệ thống
     * và bàn phím để nội dung không bị che. Dùng binding.root, KHÔNG dùng findViewById.
     */
    private fun applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    /** Lắng nghe nút chuyển tab Login <-> Register và cập nhật giao diện. */
    private fun setupModeToggle() {
        binding.toggleAuthMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener // Bỏ qua sự kiện bỏ chọn

            isRegisterMode = (checkedId == binding.btnTabRegister.id)

            // Ô xác nhận mật khẩu chỉ hiện khi Đăng ký
            binding.tilConfirmPassword.visibility =
                if (isRegisterMode) View.VISIBLE else View.GONE

            // Link Quên mật khẩu chỉ hiện khi Đăng nhập
            binding.tvForgotPassword.visibility =
                if (isRegisterMode) View.GONE else View.VISIBLE

            binding.btnSubmit.text = getString(
                if (isRegisterMode) R.string.btn_register else R.string.btn_login
            )

            clearErrors()
        }
    }

    /** Nút gửi: kiểm tra dữ liệu phía client rồi mới gọi Firebase. */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (!validateInput(email, password, confirm)) return@setOnClickListener

            if (isRegisterMode) registerUser(email, password) else loginUser(email, password)
        }
    }

    /**
     * Kiểm tra dữ liệu nhập. Trả về true nếu hợp lệ.
     * Bắt lỗi: email trống/sai định dạng, mật khẩu < 6 ký tự, xác nhận mật khẩu không khớp.
     */
    private fun validateInput(email: String, password: String, confirm: String): Boolean {
        clearErrors()
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format (e.g., name@gmail.com)"
            isValid = false
        }

        // Firebase yêu cầu mật khẩu tối thiểu 6 ký tự
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (isRegisterMode && password != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }
        return isValid
    }

    /** Đăng ký tài khoản mới. */
    private fun registerUser(email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Đăng ký thành công: Firebase tự đăng nhập luôn user mới
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /** Đăng nhập bằng tài khoản đã có. */
    private fun loginUser(email: String, password: String) {
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /** BONUS 1: gửi email đặt lại mật khẩu tới địa chỉ người dùng đã nhập. */
    private fun setupForgotPassword() {
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            clearErrors()

            // Phải có email hợp lệ mới gửi được
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Enter a valid email to receive the password reset link"
                return@setOnClickListener
            }

            showLoading(true)
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Sent a password reset link to $email. Please check your inbox (including Spam).",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    handleAuthError(task.exception)
                }
            }
        }
    }

    /** Chuyển exception của Firebase thành thông báo thân thiện. */
    private fun handleAuthError(exception: Exception?) {
        Log.e(TAG, "Firebase auth error", exception) // Xem chi tiết trong Logcat
        when (exception) {
            // Mật khẩu yếu (WEAK_PASSWORD) - phải kiểm tra TRƯỚC InvalidCredentials vì là lớp con
            is FirebaseAuthWeakPasswordException ->
                binding.tilPassword.error = "Password is too weak, please use at least 6 characters"

            // Email sai định dạng hoặc sai thông tin đăng nhập
            is FirebaseAuthInvalidCredentialsException ->
                if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                    binding.tilEmail.error = "Invalid email format"
                } else {
                    Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_LONG).show()
                }

            // Email đã được đăng ký
            is FirebaseAuthUserCollisionException ->
                binding.tilEmail.error = "This email is already registered, please switch to Login"

            // Tài khoản không tồn tại hoặc bị vô hiệu hoá
            is FirebaseAuthInvalidUserException ->
                Toast.makeText(this, "Account does not exist or has been disabled", Toast.LENGTH_LONG).show()

            // Mất kết nối mạng
            is FirebaseNetworkException ->
                Toast.makeText(this, "No network connection, please check again", Toast.LENGTH_LONG).show()

            // Lỗi khác: hiện message gốc để dễ debug
            else ->
                Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /** Bật/tắt vòng tiến trình và khoá nút để tránh bấm nhiều lần. */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
    }

    /** Xoá thông báo lỗi trên các ô nhập. */
    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    /** Sang MainActivity và xoá back stack để nút Back không quay về màn đăng nhập. */
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "AuthActivity"
    }
}
