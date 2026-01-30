package com.tonio.albarapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DASHBOARD = "dashboard"
    const val NEW_WORKSLIP = "new_workslip"
    const val MANAGER_APPROVALS = "manager_approvals"
    const val WORKSLIP_DETAIL = "workslip_detail/{workSlipId}"
    const val USER_MANAGEMENT = "user_management"
}

fun navigateToWorkSlipDetail(workSlipId: String): String {
    return "workslip_detail/$workSlipId"
}
data class DrawerItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val baseDrawerItems = listOf(
    DrawerItem("Work Slips", Routes.DASHBOARD, Icons.Filled.Home),
    DrawerItem("New Work Slip", Routes.NEW_WORKSLIP, Icons.Filled.Add),
    DrawerItem("Manage Users", Routes.USER_MANAGEMENT, Icons.Filled.People)
)

val managerDrawerItems = listOf(
    DrawerItem("Aprobaciones", Routes.MANAGER_APPROVALS, Icons.Filled.VerifiedUser)
)