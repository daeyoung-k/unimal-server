package com.unimal.board.service.posts.manager

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.BoardFileRepository
import com.unimal.board.domain.board.BoardRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component

@Component
class PostsManager(
    private val boardFileRepository: BoardFileRepository,
    private val boardRepository: BoardRepository,
    private val geometryFactory: GeometryFactory
) {

    fun savedBoard(
        board: Board
    ) = boardRepository.save(board)

    fun savedBoardFile(
        boardFile: BoardFile
    ) = boardFileRepository.save(boardFile)

    fun createLocationPointInfo(
        longitude: Double?,
        latitude: Double?
    ): Point? {
        return if (longitude != null && latitude != null) {
            geometryFactory.createPoint(
                Coordinate(longitude, latitude)
            )
        } else null
    }
}