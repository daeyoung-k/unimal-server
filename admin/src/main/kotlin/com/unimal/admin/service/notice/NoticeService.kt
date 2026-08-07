package com.unimal.admin.service.notice

import com.unimal.admin.domain.notice.Notice
import com.unimal.admin.domain.notice.NoticeRepository
import com.unimal.common.enums.notice.NoticeType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 공지사항 관리.
 *
 * 공지는 **앱에서 운영자의 말로 읽히는 자리**라 아무나 쓸 수 없어야 한다. 그래서 작성
 * 경로를 어드민 한 곳으로 모은다 (`board` 의 공개 작성 API 는 함께 제거했다).
 */
@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
) {

    @Transactional(readOnly = true)
    fun getNotices(
        page: Int,
        size: Int,
        condition: NoticeSearchCondition = NoticeSearchCondition()
    ): Page<Notice> {
        val normalizedCondition = condition.normalized()
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            normalizedCondition.sort.toSort()
        )

        return noticeRepository.findAll(normalizedCondition.toSpecification(), pageable)
    }

    @Transactional(readOnly = true)
    fun getNotice(noticeId: Long): Notice {
        return noticeRepository.findById(noticeId)
            .orElseThrow { NoSuchElementException("Notice not found: $noticeId") }
    }

    @Transactional
    fun create(type: NoticeType, title: String, content: String): Notice {
        return noticeRepository.save(
            Notice(
                type = type,
                title = title.trim(),
                content = content.trim(),
            )
        )
    }

    @Transactional
    fun update(noticeId: Long, type: NoticeType, title: String, content: String): Notice {
        val notice = getNotice(noticeId)
        notice.update(type = type, title = title.trim(), content = content.trim())

        return notice
    }

    /** 앱에서 감춘다. 행은 남는다 — [Notice.show] KDoc 참고. */
    @Transactional
    fun hide(noticeId: Long) {
        getNotice(noticeId).hide()
    }

    @Transactional
    fun restore(noticeId: Long) {
        getNotice(noticeId).restore()
    }

    @Transactional(readOnly = true)
    fun countHidden(): Long {
        return noticeRepository.count(
            NoticeSearchCondition(show = false).toSpecification()
        )
    }

    private fun NoticeSearchCondition.toSpecification(): Specification<Notice> =
        Specification { root, _, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            type?.let {
                predicates.add(criteriaBuilder.equal(root.get<NoticeType>("type"), it))
            }

            show?.let {
                predicates.add(criteriaBuilder.equal(root.get<Boolean>("show"), it))
            }

            keyword?.let {
                val pattern = "%${it.lowercase()}%"
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern)
                    )
                )
            }

            if (predicates.isEmpty()) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
}
