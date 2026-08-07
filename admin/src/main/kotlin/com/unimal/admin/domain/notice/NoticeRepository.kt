package com.unimal.admin.domain.notice

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface NoticeRepository :
    JpaRepository<Notice, Long>,
    JpaSpecificationExecutor<Notice>
