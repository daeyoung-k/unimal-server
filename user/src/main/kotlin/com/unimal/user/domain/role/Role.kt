package com.unimal.user.domain.role

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "role")
open class Role(

    @Column(unique = true, nullable = false)
    val name: String

) : BaseIdEntity()