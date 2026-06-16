INSERT INTO admin_member (id, login_id, password, name, email, status, created_at)
VALUES (1, 'admin', '$2y$10$I.sm7VfDsV4c00rPdli5meOTHpygllpEZnzN0moYZuTETpPg.KhbS', '테스트 관리자', 'admin@unimal.co.kr', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO admin_member_role (id, admin_member_id, role)
VALUES (1, 1, 'SUPER_ADMIN');

INSERT INTO unimal_user.member (id, email, nickname, name, provider, status, profile_image, introduction, created_at, updated_at)
VALUES
    (1, 'leaf@unimal.co.kr', '리프', '김리프', 'KAKAO', 'ACTIVE', 'https://cdn.unimal.co.kr/profile/leaf.png', '안녕하세요. 리프입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'river@unimal.co.kr', '리버', '박리버', 'NAVER', 'WITHDRAWAL', null, null, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));
