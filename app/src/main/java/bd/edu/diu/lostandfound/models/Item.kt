package bd.edu.diu.lostandfound.models

data class Item(
    var id: String = "",
    val title: String = "",
    val type: String = "", // "Lost" or "Found"
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val postedByUserId: String = "",
    val posterName: String = "",
    val posterContact: String = "",
    var status: String = "Active", // "Active" or "Recovered"
    val timestamp: Long = System.currentTimeMillis()
)
