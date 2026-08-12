INSERT INTO admin_member (id, login_id, password, name, email, status, created_at)
VALUES (1, 'admin', '$2y$10$I.sm7VfDsV4c00rPdli5meOTHpygllpEZnzN0moYZuTETpPg.KhbS', '테스트 관리자', 'admin@unimal.co.kr', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO admin_member_role (id, admin_member_id, role)
VALUES (1, 1, 'SUPER_ADMIN');

INSERT INTO unimal_user.member (id, email, nickname, name, provider, status, profile_image, introduction, created_at, updated_at)
VALUES
    (1, 'leaf@unimal.co.kr', '리프', '김리프', 'KAKAO', 'ACTIVE', 'https://cdn.unimal.co.kr/profile/leaf.png', '안녕하세요. 리프입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'river@unimal.co.kr', '리버', '박리버', 'NAVER', 'WITHDRAWAL', null, null, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- 게시판 관리 화면용. 1번은 사진 두 장, 2번은 사진 없음·삭제됨 — 썸네일 유무/삭제 배지 분기를 모두 렌더해 본다.
INSERT INTO unimal_board.board (id, email, title, content, street_name, postal_code, si_do, gu_gun, dong, show, del, created_at, updated_at)
VALUES
    (1, 'leaf@unimal.co.kr', '역삼동 맛집', '점심에 다녀왔어요.', '서울 강남구 테헤란로 123', '06133', '서울', '강남구', '역삼동', 'PUBLIC', false, CURRENT_TIMESTAMP, null),
    (2, 'river@unimal.co.kr', null, '제목 없는 글입니다.', null, null, null, null, null, 'PRIVATE', true, DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

INSERT INTO unimal_board.board_file (id, board_id, main, file_url, thumb_url)
VALUES
    (1, 1, true, 'https://cdn.unimal.co.kr/board/1/a.jpg', 'https://cdn.unimal.co.kr/board/1/a_thumb.jpg'),
    (2, 1, false, 'https://cdn.unimal.co.kr/board/1/b.jpg', null);
