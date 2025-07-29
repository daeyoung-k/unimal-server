package com.unimal.user.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmailAndProvider(email: String, provider: String): Member?
    fun findByEmail(email: String): Member?
    fun findByTel(tel: String): Member?
    fun findByNickname(nickname: String): Member?
}