package com.unimal.user.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
open class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 20)
    val name: String? = null,

    @Column(length = 30)
    val nickname: String? = null,

    @Column(length = 50)
    val email: String? = null,

    @Column(length = 20)
    val tel: String? = null,

    @Column(length = 10)
    val provider: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null,
)