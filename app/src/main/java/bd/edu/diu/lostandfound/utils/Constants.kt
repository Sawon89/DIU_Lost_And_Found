package bd.edu.diu.lostandfound.utils

object Constants {
    val CATEGORIES = arrayOf("ID Card", "Mobile Phone", "Wallet", "Bag", "Book", "Calculator", "Keys", "Other")
    val LOCATIONS = arrayOf("New Campus", "Old Campus", "Library", "Cafeteria", "Classroom", "Laboratory", "Playground", "Other")

    fun isValidStudentId(studentId: String): Boolean {
        val pattern = Regex("^[A-Za-z0-9\\-]{4,25}$")
        return studentId.length >= 4 && pattern.matches(studentId)
    }
}
