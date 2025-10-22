package com.unimal.board.service.posts

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.BoardFileRepository
import com.unimal.board.domain.board.BoardRepository
import org.springframework.stereotype.Component

@Component
class BoardManager(
    private val boardFileRepository: BoardFileRepository,
    private val boardRepository: BoardRepository
) {

    fun savedBoard(
        board: Board
    ) = boardRepository.save(board)

    fun savedBoardFile(
        boardFile: BoardFile
    ) = boardFileRepository.save(boardFile)
}