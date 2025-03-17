package com.unimal.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "test")
open class Test(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String? = null,

    val email: String? = null
)