package com.unimal.board.service.posts.manager

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

    fun saveBoard(
        board: Board
    ) = boardRepository.save(board)

    fun saveBoardFile(
        boardFile: BoardFile
    ) = boardFileRepository.save(boardFile)
}