package com.unimal.user.domain.member

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "member_device")
open class MemberDevice(

    @ManyToOne
    @JoinColumn(name = "member_id")
    var member: Member,
    var fcmToken: String? = null,
    var model: String? = null,
    var systemName: String? = null,
    var systemVersion: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null

) : BaseIdEntity() {
    fun fcmTokenUpdate(
        fcmToken: String?
    ) {
        if (!fcmToken.isNullOrBlank() && this.fcmToken != fcmToken) {
            this.fcmToken = fcmToken
            this.updatedAt = LocalDateTime.now()
        }
    }

    fun modelUpdate(
        model: String?
    ) {
        if (!model.isNullOrBlank() && this.model != model) {
            this.model = model
            this.updatedAt = LocalDateTime.now()
        }
    }

    fun systemNameUpdate(
        systemName: String?
    ) {
        if (!systemName.isNullOrBlank() && this.systemName != systemName) {
            this.systemName = systemName
            this.updatedAt = LocalDateTime.now()
        }
    }

    fun systemVersionUpdate(
        systemVersion: String?
    ) {
        if (!systemVersion.isNullOrBlank() && this.systemVersion != systemVersion) {
            this.systemVersion = systemVersion
            this.updatedAt = LocalDateTime.now()
        }
    }
}