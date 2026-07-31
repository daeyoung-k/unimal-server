# DB 인덱스·제약 백로그

`ddl-auto: update` 를 쓰고 마이그레이션 도구가 없어서, 인덱스·제약·데이터 마이그레이션은
손으로 실행해야 한다. **"운영에 적용했는지" 를 기억에 의존하지 않기 위해** 여기에 기록한다.

적용할 때 체크박스를 채우고 날짜를 남긴다.

> 🔴 근본 해결책은 **Flyway 도입**이다. 아래 § 후속 참고.

---

## 2026-07-29 — 지도 바텀카드 피드

관련 문서: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §7

### 1. ~~NEARBY 섹션 인덱스~~ — 취소 (2026-07-29)

```sql
-- 만들지 않는다
-- CREATE INDEX CONCURRENTLY idx_board_area_created
--     ON board (si_do, gu_gun, dong, created_at DESC)
--     WHERE del = false AND show::text = 'PUBLIC';
```

`si_do + gu_gun + dong` 으로 행정동 글을 뽑는 `NEARBY` 섹션 전용 인덱스였다.
**설계가 바뀌어 행정동 섹션 자체가 없어졌으므로 불필요하다.**

피드는 반경·행정동 필터를 버리고 **거리를 정렬 기준으로만** 쓰는 단일 `NEAR` 섹션이 됐다
(`ORDER BY location <-> point`, PostGIS KNN). 이유는 스펙 §1 — 출시 후 홍보를 안 해서
글이 거의 없는 상태에서 반경/행정동 필터를 걸면 피드가 백지가 된다.

KNN 정렬은 기존 `idx_board_location_gist` 를 그대로 탄다. **신규 인덱스가 필요 없다.**

> 나중에 밀도가 올라 행정동 기반 섹션을 다시 도입하면 이 인덱스를 되살린다.
> 그때 `del`/`show` predicate 를 `idx_board_location_gist` 와 동일하게 맞출 것
> (두 인덱스 조건이 같아야 쿼리를 고칠 때 한쪽만 인덱스를 놓치는 사고가 안 난다).

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

## 실제 인덱스 목록 (2026-07-29 **로컬** `pg_indexes` 실측)

> 🔴 **이 문서의 이전 판에 적힌 인덱스 목록은 틀렸었다.** 실물 확인 없이 작성된 것으로,
> 이름과 정의가 대부분 실제와 다르다. 아래가 실측값이다.

| 테이블 | 인덱스 | 정의 |
| --- | --- | --- |
| `board` | `idx_board_location` | `GIST (location)` — **predicate 없음** |
| `board` | `idx_board_email_del` | `btree (email, del)` |
| `board` | `idx_board_show_del_created` | `btree (show, del, created_at DESC)` |
| `board_file` | `idx_board_file_board_main_id` | `btree (board_id, main DESC, id)` |
| `board_like` | `idx_board_like_board_id` | `btree (board_id)` |
| `board_like` | `idx_board_like_board_email` | `UNIQUE btree (board_id, email)` |
| `board_like` | `idx_board_like_email` | `btree (email)` |
| `board_reply` | `idx_board_reply_board_del` | `btree (board_id, del)` |
| `board_reply` | `idx_board_reply_board_reply_created` | `btree (board_id, reply_id, created_at)` |
| `board_reply` | `idx_board_reply_reply_id` | `btree (reply_id)` |
| `board_reply` | `idx_board_reply_id_board_email` | `btree (id, board_id, email)` |
| `board_member` | `idx_board_member_email` | `UNIQUE btree (email)` |
| `board_member` | `ukib2kl3vjf09y34gk8g8i2n9d0` | `UNIQUE btree (email)` — **위와 완전 중복** |
| `board_member` | `idx_board_member_status` | `btree (status)` |

### 이전 판이 틀렸던 항목

| 이전 주장 | 실제 |
| --- | --- |
| `idx_board_location_gist ... WHERE del=false AND show::text='PUBLIC'` | `idx_board_location` — **부분 인덱스가 아니다** |
| `idx_board_reply_board (board_id) WHERE del=false` | 존재하지 않음. `idx_board_reply_board_del (board_id, del)` 복합 인덱스 |
| `idx_board_file_board (board_id)` + "복합 인덱스는 만들지 않기로 결정" | `idx_board_file_board_main_id (board_id, main DESC, id)` — **이미 존재하고 실제로 쓰인다** |
| `idx_board_like_email (email, board_id)` | `idx_board_like_email (email)` — 단일 컬럼 |

### ⚠️ 운영 DB 대조가 필요하다

위는 **로컬 `unimal-postgis` 컨테이너** 실측이다. 이전 판은 "운영 DB 실물 확인" 이라고
적혀 있었는데 로컬과 이렇게 다르면 그 확인을 신뢰할 수 없다.

- [ ] 운영 DB 에서 아래 쿼리 실행해 위 표와 대조

```sql
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'unimal_board'
  AND tablename IN ('board','board_file','board_like','board_reply','board_member')
ORDER BY tablename, indexname;
```

---

## 신규 백로그 (2026-07-29 실측에서 발견, 급하지 않음)

### A. `board_member(email)` 중복 UNIQUE 인덱스 제거

`idx_board_member_email` 과 `ukib2kl3vjf09y34gk8g8i2n9d0` 이 둘 다 `UNIQUE btree (email)` 이다.
완전 중복이라 **INSERT/UPDATE 마다 같은 일을 두 번 한다.**

```sql
-- 손으로 만든 쪽을 남기고 Hibernate 자동 생성분을 지우는 게 낫다
-- (이름이 읽히므로). 단 엔티티의 @Column(unique = true) 를 지우지 않으면
-- ddl-auto 가 다시 만든다 — 엔티티 수정과 함께 해야 한다.
DROP INDEX IF EXISTS unimal_board.ukib2kl3vjf09y34gk8g8i2n9d0;
```

- [ ] 운영 확인 (로컬만의 문제일 수 있다)
- [ ] `BoardMember` 엔티티의 `@Column(unique = true)` 제거와 함께 처리

### B. `idx_board_location` 을 부분 인덱스로 전환 — **지금은 하지 않는다**

현재 GiST 인덱스는 비공개·삭제 글까지 담는다. 마커·피드 쿼리는 항상
`del = false AND show = 'PUBLIC'` 을 걸므로, 인덱스가 공개글만 담으면 트리가 작아지고
KNN 스캔이 버릴 행을 만나지 않는다.

```sql
-- 측정 가능한 문제가 생긴 뒤에 한다
CREATE INDEX CONCURRENTLY idx_board_location_public
    ON board USING gist (location)
    WHERE del = false AND show::text = 'PUBLIC';
```

**지금 하는 건 이르다.** 전체 29건 중 제외 대상이 6건이라 이득이 측정되지 않는다.
글이 수만 건이 되고 비공개 비율이 올라가면 재검토한다.

전환 시 주의: predicate 를 쿼리와 문자 그대로 맞춰야 플래너가 함의를 증명한다.
`show` 는 `(show)::text = 'PUBLIC'::text` 형태로 쓰고, 쿼리도 문자열 비교여야 한다.

### C. `board_reply.del` NOT NULL 의 근거 정정 (기록용, 조치 불필요)

`1fe7a37` 커밋은 전환 이유로 "부분 인덱스 `idx_board_reply_board(board_id) WHERE del=false`
의 함의 증명 실패 → 전체 스캔" 을 들었는데, **그 부분 인덱스는 존재하지 않는다.**
실제는 `idx_board_reply_board_del (board_id, del)` 복합 인덱스이고, 복합 인덱스는
선행 컬럼 `board_id` 로 스캔하므로 `COALESCE(del, false)` 여도 인덱스를 탄다.

**결론(NOT NULL 전환)은 맞고 근거만 틀렸다.** nullable Boolean 은 그 자체로 나쁜
모델링이고 `COALESCE` 방어 코드가 걷혀서 코드가 단순해졌으므로 되돌리지 않는다.

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
