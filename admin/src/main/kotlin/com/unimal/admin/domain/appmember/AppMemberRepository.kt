package com.unimal.admin.domain.appmember

import org.springframework.data.jpa.repository.JpaRepository

interface AppMemberRepository : JpaRepository<AppMember, Long>
