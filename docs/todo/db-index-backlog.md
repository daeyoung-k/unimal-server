# DB 인덱스·제약 백로그

`ddl-auto: update` 를 쓰고 마이그레이션 도구가 없어서, 인덱스·제약·데이터 마이그레이션은
손으로 실행해야 한다. **"운영에 적용했는지" 를 기억에 의존하지 않기 위해** 여기에 기록한다.

적용할 때 체크박스를 채우고 날짜를 남긴다.

> 🔴 근본 해결책은 **Flyway 도입**이다. 아래 § 후속 참고.

---

## 2026-07-29 — 지도 바텀카드 피드

관련 문서: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §7

### 1. NEARBY 섹션 인덱스 (신규)

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_board_area_created
    ON board (si_do, gu_gun, dong, created_at DESC)
    WHERE del = false AND show::text = 'PUBLIC';
```

- [ ] 로컬
- [ ] 운영

`del`/`show` predicate 를 `idx_board_location_gist` 와 **동일하게** 맞췄다.
두 인덱스 조건이 같아야 쿼리를 고칠 때 한쪽만 인덱스를 놓치는 사고가 안 난다.

`CONCURRENTLY` 는 트랜잭션 블록 안에서 실행할 수 없다. psql 에서 단독 실행.

### 2. `board_reply.del` NOT NULL 제약

```sql
-- 백필: 2026-07-29 확인 시 대상 0건 (no-op 이지만 순서상 먼저 실행)
UPDATE board_reply SET del = false WHERE del IS NULL;

ALTER TABLE board_reply ALTER COLUMN del SET DEFAULT false;
ALTER TABLE board_reply ALTER COLUMN del SET NOT NULL;
```

- [ ] 로컬
- [ ] 운영

**왜 필요한가:** `del` 이 nullable 이면 조회 조건에 `coalesce(del, false) = false` 를
써야 하는데, 그러면 부분 인덱스

```
idx_board_reply_board  btree (board_id) WHERE (del = false)
```

의 predicate 함의를 플래너가 증명할 수 없어 **인덱스를 버리고 `board_reply` 전체 스캔**을 한다.
`del = false` 로 쓰려면 NULL 이 없어야 한다.

코드 쪽 대응은 이미 완료 (`BoardReply` 엔티티 non-null, `coalesce` 제거).
**엔티티가 `nullable = false` 라 `ddl-auto` 가 제약을 만들 수도 있지만 믿지 않는다** —
Hibernate 의 `update` 모드는 기존 컬럼의 nullability 를 변경해 주지 않는다.
위 SQL 을 직접 실행해야 한다.

---

## 이미 운영에 존재하는 것 (2026-07-29 확인, 조치 불필요)

확인만 하고 남겨두는 기록. 같은 조사를 반복하지 않기 위해.

| 테이블 | 인덱스 | 정의 |
| --- | --- | --- |
| `board` | `idx_board_location_gist` | `GIST (location) WHERE del = false AND show::text = 'PUBLIC'` |
| `board_file` | `idx_board_file_board` | `btree (board_id)` |
| `board_like` | `idx_board_like_board` | `btree (board_id)` |
| `board_like` | `idx_board_like_email` | `btree (email, board_id)` |
| `board_reply` | `idx_board_reply_board` | `btree (board_id) WHERE del = false` |
| `board_member` | `ukib2kl3vjf09y34gk8g8i2n9d0` | `UNIQUE btree (email)` — Hibernate 자동 생성 |

### 만들지 않기로 결정한 것

`board_file (board_id, main DESC, id)` — LATERAL 의 `ORDER BY main DESC, id ASC LIMIT 1`
용으로 검토했으나 **불필요**하다고 판단.

- 글 하나당 사진은 많아도 10장 수준. 기존 `(board_id)` 인덱스로 몇 행 가져와 메모리
  정렬하는 비용은 사실상 0.
- 결정적으로 `file_url` 이 인덱스에 없어 **힙 접근이 어차피 일어난다.** index-only scan
  이 안 되므로 복합 인덱스로 얻는 게 없다.

쓰기 비용만 늘고 읽기 이득이 없다.

---

## 후속 — Flyway 도입 (우선순위 높음)

이 문서 자체가 임시방편이다. 체크박스를 채우는 걸 잊으면 그대로 무용지물이 된다.

**1인 개발자에게 Flyway 는 오버엔지니어링이 아니라 오히려 필수급이다.**
검토해 줄 동료가 없어서 "적용 안 된 인덱스" 를 잡아줄 사람도 없다.

시작은 가볍다.

1. `implementation("org.flywaydb:flyway-core")` + `flyway-database-postgresql`
2. `ddl-auto: validate` 로 변경 (`update` 는 스키마를 몰래 바꿔서 마이그레이션과 충돌한다)
3. 현재 스키마를 `V1__baseline.sql` 로 덤프 (`pg_dump --schema-only`)
4. 운영 DB 에 `flyway baseline` 로 V1 을 "이미 적용됨" 처리
5. 이후 변경은 `V2__add_board_area_index.sql` 처럼 파일로만

부수 효과로 `ukib2kl3vjf09y34gk8g8i2n9d0` 같은 해시 이름 인덱스도 명시적 이름으로
관리하게 된다.
