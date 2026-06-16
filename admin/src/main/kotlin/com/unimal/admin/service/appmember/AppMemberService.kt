package com.unimal.admin.service.appmember

import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.domain.appmember.AppMemberRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppMemberService(
    private val appMemberRepository: AppMemberRepository
) {

    @Transactional(readOnly = true)
    fun getMembers(page: Int, size: Int): Page<AppMember> {
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        return appMemberRepository.findAll(pageable)
    }
}
