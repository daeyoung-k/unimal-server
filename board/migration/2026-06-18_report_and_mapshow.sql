-- ============================================================
-- 신고 접수 기능 + map_show 제거 마이그레이션
-- 대상 스키마: unimal_board
-- 실행 시점: 신규 코드 배포 전/후 (각 단계 주석 참고)
--
-- ⚠️ ddl-auto=update 는 컬럼 추가/테이블 생성만 자동 반영한다.
--    아래 항목(DROP, NOT NULL 변경, UNIQUE 제약)은 수동 실행 필수.
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. map_show 제거 (무중단 안전 순서)
-- ────────────────────────────────────────────────────────────

-- 1-1) [배포 전] map_show NOT NULL 해제
--      → 이 시점부터 mapShow 필드 없는 신규 코드가 INSERT 해도 NULL 허용되어 안전.
ALTER TABLE unimal_board.board ALTER COLUMN map_show DROP NOT NULL;

-- 1-2) [신규 코드 배포]  (엔티티에 mapShow 없음)

-- 1-3) [배포 안정화 후, 언제든] 컬럼 완전 제거
ALTER TABLE unimal_board.board DROP COLUMN map_show;

-- 1-4) show 컬럼에 잔존 'SAME' 값 점검 (PostShow.SAME 제거에 따른 enum 파싱 에러 방지)
--      기본값이 PUBLIC이라 보통 0건이지만 확인 권장.
--      SELECT count(*) FROM unimal_board.board WHERE show = 'SAME';
--      있으면: UPDATE unimal_board.board SET show = 'PUBLIC' WHERE show = 'SAME';


-- ────────────────────────────────────────────────────────────
-- 2. report 테이블 - 중복 신고 방지 UNIQUE 제약
--    (이미 존재하는 report 테이블엔 ddl-auto가 제약을 자동 추가하지 않으므로 수동 실행)
-- ────────────────────────────────────────────────────────────

-- 2-1) 기존 중복 데이터 점검 (있으면 정리 후 제약 추가)
--      SELECT reporter_email, target_type, target_id, count(*)
--      FROM unimal_board.report
--      GROUP BY reporter_email, target_type, target_id
--      HAVING count(*) > 1;

-- 2-2) UNIQUE 제약 추가
ALTER TABLE unimal_board.report
    ADD CONSTRAINT uk_report_reporter_target UNIQUE (reporter_email, target_type, target_id);

-- 2-3) reason / status enum 값 정비 점검
--      기존 status 값(REVIEWED/DISMISSED/SUCCESSED)이나 잘못된 reason 이 있으면 매핑 필요.
--      신규 기능이라 보통 0건.
--      SELECT count(*) FROM unimal_board.report WHERE status IN ('REVIEWED','DISMISSED','SUCCESSED');
--      있으면: UPDATE unimal_board.report SET status = 'PENDING' WHERE status IN ('REVIEWED','DISMISSED','SUCCESSED');

-- ※ report.reviewed_by 컬럼은 ddl-auto=update 가 자동 추가하므로 수동 작업 불필요.
