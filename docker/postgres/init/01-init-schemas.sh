#!/bin/bash
set -e

# Unimal 프로젝트 스키마 초기화 스크립트
# 이 스크립트는 컨테이너가 처음 시작될 때 자동으로 실행됩니다.

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- PostGIS 확장 활성화 (이미 활성화되어 있지만 명시적으로 확인)
    CREATE EXTENSION IF NOT EXISTS postgis;
    CREATE EXTENSION IF NOT EXISTS postgis_topology;

    -- 스키마 생성
    CREATE SCHEMA IF NOT EXISTS unimal_user;
    CREATE SCHEMA IF NOT EXISTS unimal_map;
    CREATE SCHEMA IF NOT EXISTS unimal_board;
    CREATE SCHEMA IF NOT EXISTS unimal_photo;
    CREATE SCHEMA IF NOT EXISTS unimal_notification;

    -- 스키마 소유자 설정
    ALTER SCHEMA unimal_user OWNER TO CURRENT_USER;
    ALTER SCHEMA unimal_map OWNER TO CURRENT_USER;
    ALTER SCHEMA unimal_board OWNER TO CURRENT_USER;
    ALTER SCHEMA unimal_photo OWNER TO CURRENT_USER;
    ALTER SCHEMA unimal_notification OWNER TO CURRENT_USER;

    -- 기본 권한 설정 (향후 생성될 테이블에 대한 권한)
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_user GRANT ALL ON TABLES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_map GRANT ALL ON TABLES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_board GRANT ALL ON TABLES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_photo GRANT ALL ON TABLES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_notification GRANT ALL ON TABLES TO CURRENT_USER;

    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_user GRANT ALL ON SEQUENCES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_map GRANT ALL ON SEQUENCES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_board GRANT ALL ON SEQUENCES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_photo GRANT ALL ON SEQUENCES TO CURRENT_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA unimal_notification GRANT ALL ON SEQUENCES TO CURRENT_USER;

    -- PostGIS 공간 기능 공유를 위한 search_path 설정
    -- public 스키마에 설치된 PostGIS 확장을 모든 스키마에서 사용할 수 있도록 설정
    -- "\$user"는 현재 접속한 유저 이름과 같은 스키마가 있다면 최우선으로 참조
    ALTER DATABASE $POSTGRES_DB SET search_path TO "\$user", unimal_user, unimal_map, unimal_board, unimal_photo, unimal_notification, public;
EOSQL

echo "✓ 스키마 초기화 완료: unimal_user, unimal_map, unimal_board, unimal_photo, unimal_notification"
echo "✓ search_path 설정 완료: PostGIS 공간 기능을 모든 스키마에서 사용 가능"
