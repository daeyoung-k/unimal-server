package com.unimal.admin.domain.appmember

import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AppMemberRepository : JpaRepository<AppMember, Long>, JpaSpecificationExecutor<AppMember> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            update unimal_user.member
            set profile_image = null,
                updated_at = :updatedAt
            where id = :id
        """,
        nativeQuery = true
    )
    fun resetProfileImage(
        @Param("id") id: Long,
        @Param("updatedAt") updatedAt: LocalDateTime
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            update unimal_user.member
            set introduction = null,
                updated_at = :updatedAt
            where id = :id
        """,
        nativeQuery = true
    )
    fun hideIntroduction(
        @Param("id") id: Long,
        @Param("updatedAt") updatedAt: LocalDateTime
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            update unimal_user.member
            set status = :status,
                updated_at = :updatedAt
            where id = :id
        """,
        nativeQuery = true
    )
    fun updateStatus(
        @Param("id") id: Long,
        @Param("status") status: String,
        @Param("updatedAt") updatedAt: LocalDateTime
    ): Int
}
