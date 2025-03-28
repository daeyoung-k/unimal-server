package com.unimal.common.domain

import jakarta.persistence.*


@MappedSuperclass
open class BaseIdEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null
)