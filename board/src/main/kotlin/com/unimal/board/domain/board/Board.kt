package com.unimal.board.domain.board

import com.unimal.board.domain.member.BoardMember
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board")
open class Board(

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email")
    val email: BoardMember,

    @Column(columnDefinition = "geography(Point, 4326)")
    val location: Point? = null,

    val title: String? = null,

    @Column(columnDefinition = "text", nullable = false)
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
    val public: Boolean? = false,
    val del: Boolean? = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,


    ) : BaseIdEntity()