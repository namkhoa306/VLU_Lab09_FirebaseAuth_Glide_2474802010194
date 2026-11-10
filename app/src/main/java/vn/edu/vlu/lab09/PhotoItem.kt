package vn.edu.vlu.lab09

/**
 * PhotoItem: model dữ liệu cho một ảnh trong danh sách.
 * data class tự sinh equals(), hashCode(), toString(), copy().
 */
data class PhotoItem(
    val id: Int,          // Mã ảnh
    val title: String,    // Tiêu đề hiển thị
    val author: String,   // Tên tác giả
    val imageUrl: String  // Đường dẫn ảnh (BẮT BUỘC https://)
)

/**
 * PhotoDataSource: nguồn dữ liệu giả gồm 10 ảnh chất lượng cao từ Picsum Photos.
 * Định dạng URL: https://picsum.photos/id/{id}/{width}/{height}
 */
object PhotoDataSource {

    fun getPhotos(): List<PhotoItem> = listOf(
        PhotoItem(1, "Sunrise on the mountain", "Picsum Studio", "https://picsum.photos/id/10/800/600"),
        PhotoItem(2, "Foggy pine forest", "Picsum Studio", "https://picsum.photos/id/11/800/600"),
        PhotoItem(3, "Sunset beach", "Picsum Studio", "https://picsum.photos/id/12/800/600"),
        PhotoItem(4, "Autumn road", "Picsum Studio", "https://picsum.photos/id/14/800/600"),
        PhotoItem(5, "Peaceful lake", "Picsum Studio", "https://picsum.photos/id/15/800/600"),
        PhotoItem(6, "Deep blue ocean", "Picsum Studio", "https://picsum.photos/id/16/800/600"),
        PhotoItem(7, "Spring valley", "Picsum Studio", "https://picsum.photos/id/17/800/600"),
        PhotoItem(8, "Coastal cliff", "Picsum Studio", "https://picsum.photos/id/18/800/600"),
        PhotoItem(9, "Ripe rice field", "Picsum Studio", "https://picsum.photos/id/19/800/600"),
        PhotoItem(10, "Waterfall in the forest", "Picsum Studio", "https://picsum.photos/id/20/800/600"),
        PhotoItem(11, "City at night", "Picsum Studio", "https://picsum.photos/id/29/800/600"),
        PhotoItem(12, "Quiet harbor", "Picsum Studio", "https://picsum.photos/id/42/800/600")
    )
}
