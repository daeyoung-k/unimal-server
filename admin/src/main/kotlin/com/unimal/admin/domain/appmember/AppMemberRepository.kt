package com.unimal.admin.domain.appmember

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface AppMemberRepository : JpaRepository<AppMember, Long>, JpaSpecificationExecutor<AppMember>
