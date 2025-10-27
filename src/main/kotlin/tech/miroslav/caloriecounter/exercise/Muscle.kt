package tech.miroslav.caloriecounter.exercise

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

/** Read-only reference table of muscles with group_name */
@Entity
@Table(name = "muscle")
class Muscle(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var name: String = "",

    @Column(name = "group_name", nullable = false)
    var groupName: String = ""
)
