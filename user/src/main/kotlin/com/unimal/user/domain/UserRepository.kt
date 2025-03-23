package com.unimal.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Member, Long> {
}