package com.tonio.albarapp.data

data class Company(val id: String, val name: String)
data class Worksite(val id: String, val name: String, val address: String? = null, val city: String? = null)

object FakeEntities {
    val companies = listOf(
        Company("c1", "Construcciones Madrid S.L."),
        Company("c2", "Infraestructuras del Sur"),
        Company("c3", "Obras Civiles Levante")
    )

    val worksites = listOf(
        Worksite("w1", "Metro Line 5 Station", "Plaza Mayor", "Madrid"),
        Worksite("w2", "Warehouse Roof", "Polígono Ind. Norte", "Valencia"),
        Worksite("w3", "Bridge Maintenance – Zone A", "Río Guadalquivir", "Sevilla")
    )
}
