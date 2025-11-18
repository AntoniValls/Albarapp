package com.tonio.albarapp

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val companyId: String? = null // Links to Company
)

enum class UserRole {
    CONTRACTOR,      // Creates work slips, sends to subcontractor
    SUBCONTRACTOR,   // Receives work slips, signs first
    MANAGER          // Approves final signed slips
}

// Mock users for testing
object MockUsers {
    val contractor = User(
        id = "u1",
        name = "Antoni García",
        email = "antoni@construcciones-madrid.es",
        role = UserRole.CONTRACTOR,
        companyId = "c1"
    )

    val subcontractor = User(
        id = "u2",
        name = "Carlos Ruiz",
        email = "carlos@infraestructuras-sur.es",
        role = UserRole.SUBCONTRACTOR,
        companyId = "c2"
    )

    val manager = User(
        id = "u3",
        name = "María López",
        email = "maria@construcciones-madrid.es",
        role = UserRole.MANAGER,
        companyId = "c1"
    )

    val allUsers = listOf(contractor, subcontractor, manager)
}