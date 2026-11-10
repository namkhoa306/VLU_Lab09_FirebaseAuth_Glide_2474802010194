package vn.edu.vlu.lab09

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import vn.edu.vlu.lab09.databinding.ItemPhotoBinding

/**
 * PhotoAdapter: cầu nối giữa danh sách PhotoItem và RecyclerView.
 * Mỗi dòng được tạo từ item_photo.xml qua ItemPhotoBinding (View Binding).
 */
class PhotoAdapter(
    private val photos: List<PhotoItem>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    /** ViewHolder giữ binding của một dòng để tái sử dụng khi cuộn. */
    class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Tạo ViewHolder mới khi RecyclerView cần thêm dòng. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // RecyclerView tự gắn view vào parent
        )
        return PhotoViewHolder(binding)
    }

    /** Gán dữ liệu của phần tử tại [position] vào ViewHolder. */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        val context = holder.itemView.context

        holder.binding.tvTitle.text = photo.title
        holder.binding.tvAuthor.text = context.getString(R.string.author_format, photo.author)

        // Tải ảnh bất đồng bộ bằng Glide (chạy nền, không chặn UI thread)
        Glide.with(context)
            .load(photo.imageUrl)                                   // URL ảnh
            .placeholder(R.drawable.ic_placeholder)                 // Hiện tạm khi đang tải
            .error(R.drawable.ic_broken_image)                      // Hiện khi lỗi (URL sai, mất mạng)
            .transition(DrawableTransitionOptions.withCrossFade())  // Hiệu ứng mờ dần
            .diskCacheStrategy(DiskCacheStrategy.ALL)               // Cache cả ảnh gốc và ảnh đã biến đổi
            .into(holder.binding.ivPhoto)
    }

    override fun getItemCount(): Int = photos.size

    /**
     * Khi ViewHolder bị tái chế: huỷ request đang chờ để tránh
     * hiện nhầm ảnh vào dòng được tái sử dụng (flickering) và tiết kiệm bộ nhớ.
     */
    override fun onViewRecycled(holder: PhotoViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.binding.ivPhoto)
    }
}
