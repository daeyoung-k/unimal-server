package com.unimal.board.domain.board

import com.unimal.board.domain.member.BoardMember
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board")
open class Board(

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email")
    val email: BoardMember,

    val title: String? = null,
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,


    ) : BaseIdEntity()