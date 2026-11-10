package vn.edu.vlu.lab09

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import vn.edu.vlu.lab09.databinding.ActivityMainBinding

/**
 * MainActivity: trang chủ hiển thị danh sách ảnh (Glide), avatar tròn và nút Logout trên Toolbar.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()

        auth = FirebaseAuth.getInstance()

        // Bảo vệ: chưa đăng nhập (hết phiên / đã đăng xuất) thì quay về màn Auth
        val user = auth.currentUser
        if (user == null) {
            navigateToAuth()
            return
        }

        binding.tvWelcome.text = getString(R.string.welcome_user, user.email)

        setupRecyclerView()
        setupLogoutMenu()
        loadAvatar(user.email)
    }

    /** Edge-to-edge (targetSdk 35+): tránh Toolbar bị thanh trạng thái che. Không dùng findViewById. */
    private fun applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    /** Cấu hình RecyclerView: LayoutManager dọc + Adapter với danh sách ảnh. */
    private fun setupRecyclerView() {
        binding.rvPhotos.layoutManager = LinearLayoutManager(this)
        binding.rvPhotos.adapter = PhotoAdapter(PhotoDataSource.getPhotos())
        binding.rvPhotos.setHasFixedSize(true) // Kích thước RecyclerView không đổi theo nội dung
    }

    /** Nút Logout trên Toolbar: xoá phiên Firebase rồi quay về AuthActivity. */
    private fun setupLogoutMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut() // Xoá phiên đăng nhập và token lưu trên máy
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    navigateToAuth()
                    true
                }
                else -> false
            }
        }
    }

    /** BONUS 2: tải avatar theo email và cắt thành hình tròn bằng circleCrop(). */
    private fun loadAvatar(email: String?) {
        // Dùng email làm "seed" để mỗi tài khoản có một avatar cố định, riêng biệt
        val seed = email ?: "guest"
        val avatarUrl = "https://i.pravatar.cc/200?u=$seed"

        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_broken_image)
            .circleCrop() // Biến ảnh thành hình tròn
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivAvatar)
    }

    /** Sang AuthActivity và xoá toàn bộ back stack để không Back lại được trang chủ. */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
