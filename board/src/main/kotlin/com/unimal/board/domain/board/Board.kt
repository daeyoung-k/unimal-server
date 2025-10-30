package com.unimal.board.domain.board

import com.unimal.board.domain.member.BoardMember
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.*
import org.locationtech.jts.geom.Point
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
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,


    ) : BaseIdEntity()