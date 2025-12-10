package com.unimal.board.domain.board

import com.unimal.board.domain.member.BoardMember
import com.unimal.board.enums.MapShow
import com.unimal.board.enums.PostShow
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import jakarta.persistence.Column
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

    @Column(columnDefinition = "geography(Point, 4326)")
    val location: Point? = null,

    var title: String? = null,

    @Column(columnDefinition = "text", nullable = false)
    var content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "show", nullable = false, length = 20)
    var show: PostShow = PostShow.PUBLIC,
    @Enumerated(EnumType.STRING)
    @Column(name = "map_show", nullable = false, length = 20)
    var mapShow: MapShow = MapShow.SAME,

    val del: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @OrderBy("main desc, id asc")
    val images: MutableList<BoardFile?> = mutableListOf()


    ) : BaseIdEntity()