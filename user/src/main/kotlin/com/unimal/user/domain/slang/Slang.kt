package com.unimal.user.domain.slang

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "slang")
open class Slang(

    @Column(name = "slang", nullable = false)
    val slang: String,

    @Enumerated(EnumType.STRING)
    val type: SlangType,

) : BaseIdEntity()