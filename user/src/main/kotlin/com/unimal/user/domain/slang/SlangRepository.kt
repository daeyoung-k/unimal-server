package com.unimal.user.domain.slang

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SlangRepository: JpaRepository<Slang, Long> {

    fun findBySlangAndType(slang: String, type: SlangType): Slang?

    @Query("SELECT s.slang FROM Slang s WHERE s.type = :type")
    fun findSlangTextsByType(type: SlangType = SlangType.PROFANITY): List<String>
}