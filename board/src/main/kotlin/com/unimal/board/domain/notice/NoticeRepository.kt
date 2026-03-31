package com.unimal.board.domain.notice

import org.springframework.data.jpa.repository.JpaRepository

interface NoticeRepository: JpaRepository<Notice, Long> {

    fun findByShowOrderByIdDesc(show: Boolean = true): List<Notice>
}