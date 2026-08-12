package com.unimal.admin.config

import org.hibernate.boot.model.relational.Namespace
import org.hibernate.boot.model.relational.Sequence
import org.hibernate.mapping.Table
import org.hibernate.tool.schema.spi.SchemaFilter
import org.hibernate.tool.schema.spi.SchemaFilterProvider

/**
 * 어드민의 `ddl-auto: update` 가 **자기 스키마(`unimal_admin`)만** 만지게 제한한다.
 *
 * 어드민은 `unimal_board.board` 같은 다른 서비스 소유 테이블을 읽기 전용 엔티티로
 * 매핑한다. 필터가 없으면 hbm2ddl 이 그 테이블까지 비교 대상으로 삼아, 매핑이 실제
 * DDL 과 조금만 달라도 **남의 스키마에 ALTER 를 날린다.** 실제로 2026-08-12 에
 * `board.email` 이 varchar(50) → varchar(255) 로 조용히 넓혀진 사고가 있었다
 * (어드민 엔티티에 length 지정이 없어 기본 255 로 비교됨). 스키마 주인은 각 서비스
 * 모듈이고, 어드민은 손님이다.
 *
 * `application.yaml` 의 `hibernate.hbm2ddl.schema_filter_provider` 로 연결된다.
 * H2 통합 테스트에는 적용되지 않는다 — 테스트는 이 설정이 없는 프로파일로 돌고,
 * 오히려 어드민 엔티티로 board/user 테이블을 만들어 써야 한다.
 */
class AdminSchemaFilterProvider : SchemaFilterProvider {
    override fun getCreateFilter(): SchemaFilter = AdminOnlySchemaFilter
    override fun getDropFilter(): SchemaFilter = AdminOnlySchemaFilter
    override fun getTruncatorFilter(): SchemaFilter = AdminOnlySchemaFilter
    override fun getMigrateFilter(): SchemaFilter = AdminOnlySchemaFilter
    override fun getValidateFilter(): SchemaFilter = AdminOnlySchemaFilter
}

object AdminOnlySchemaFilter : SchemaFilter {

    /** 어드민이 소유한 스키마. `DATABASE_ADMIN_SCHEMA` 와 같아야 한다. */
    private const val OWNED_SCHEMA = "unimal_admin"

    override fun includeNamespace(namespace: Namespace): Boolean {
        // 스키마 미지정(= default_schema 적용 대상)은 어드민 소유로 본다.
        val schema = namespace.name.schema?.canonicalName ?: return true
        return schema == OWNED_SCHEMA
    }

    override fun includeTable(table: Table): Boolean {
        val schema = table.schema ?: return true
        return schema == OWNED_SCHEMA
    }

    override fun includeSequence(sequence: Sequence): Boolean {
        val schema = sequence.name.schemaName?.canonicalName ?: return true
        return schema == OWNED_SCHEMA
    }
}
