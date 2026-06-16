package com.unimal.admin.service.adminmember

import com.unimal.admin.domain.adminmember.AdminMember
import com.unimal.admin.domain.adminmember.AdminMemberRepository
import com.unimal.admin.domain.adminmember.enums.AdminMemberStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserDetailsService(
    private val adminMemberRepository: AdminMemberRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val adminMember = adminMemberRepository.findByLoginId(username)
            ?: throw UsernameNotFoundException("Admin member not found")

        if (adminMember.status != AdminMemberStatus.ACTIVE || adminMember.roles.isEmpty()) {
            throw UsernameNotFoundException("Admin member not available")
        }

        return adminMember.toUserDetails()
    }

    private fun AdminMember.toUserDetails(): UserDetails {
        val authorities = roles
            .map { "ROLE_${it.role.name}" }
            .toTypedArray()

        return User
            .withUsername(loginId)
            .password(password)
            .authorities(*authorities)
            .build()
    }
}
