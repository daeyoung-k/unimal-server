package com.unimal.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository : JpaRepository<Test, Long> {
}