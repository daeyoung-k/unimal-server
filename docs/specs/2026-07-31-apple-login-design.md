# Sign in with Apple 설계 (server)

작성일: 2026-07-31
배경: 소셜 로그인을 제공하면서 Apple 로그인이 없어 App Store 심사에서 반려(가이드라인 4.8).

## 1. 노출 정책

- **iOS에서만 애플 로그인 버튼을 노출한다.**
  - 가이드라인 4.8은 App Store 심사 기준이라 Play 스토어에는 적용되지 않는다.
  - 안드로이드에서 애플 로그인은 네이티브 시트가 없고 웹 OAuth 리디렉트 방식이라
    서버 `redirect_uri` 엔드포인트 + form_post 수신 + 딥링크 브리지가 추가로 필요하다.
  - 안드로이드 유저가 카카오/네이버/구글을 두고 애플을 고를 동기가 사실상 없다.
- 향후 리스크: iOS에서 애플로 가입한 유저가 안드로이드로 기기를 바꾸면 로그인 수단이 없어진다.
  현재 규모에서는 발생 확률이 낮고, 마이페이지 "계정 연동"으로 해결하는 것이 순서상 맞다.

## 2. API

### POST /user/auth/login/mobile/apple

인증 불필요 (게이트웨이 `/user/auth/login/**` public route에 포함).

Request

```json
{
  "identityToken": "eyJraWQiOi...",
  "authorizationCode": "c1a2b3...",
  "name": "홍길동",
  "nickname": "홍길동"
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| `identityToken` | O | 애플이 발급한 JWT. 서버가 서명/iss/aud/exp를 검증한다. |
| `authorizationCode` | X | refresh_token 교환용. 탈퇴 시 revoke에 사용. |
| `name` | X | **최초 인증 1회에만** 애플이 내려준다. 그때 저장해야 한다. |
| `nickname` | X | 미전달 시 `name`을 닉네임 기본값으로 사용한다. |

Response — 다른 소셜 로그인과 동일하게 헤더로 토큰을 내려준다.

```
X-Unimal-Email / X-Unimal-Access-Token / X-Unimal-Refresh-Token / X-Unimal-Provider
```

전화번호가 없으면 기존 소셜 로그인과 동일하게 `TelNotFoundException`(code 1009) →
앱은 전화번호 인증 화면으로 이동한다.

**이메일은 클라이언트 값을 신뢰하지 않는다.** 반드시 identityToken 검증 결과에서 꺼낸다.

## 3. 회원 식별

애플은 사용자가 "이메일 가리기"를 켜고 끄면 relay 이메일(`@privaterelay.appleid.com`)이
바뀔 수 있어 이메일이 영구 식별자가 아니다. `member.provider_id`에 identityToken의
`sub`를 저장하고 조회 우선순위를 아래와 같이 둔다.

1. `provider = 'APPLE' AND provider_id = sub`
2. `email` (provider_id 도입 이전 가입자 호환 / 이메일 공유 케이스)
3. 없으면 신규 가입

2번으로 찾은 회원이 애플 계정일 때만 `provider_id`를 채운다.
다른 provider 계정에 애플 `sub`를 덮어쓰지 않는다.

## 4. 탈퇴 시 토큰 revoke

애플은 계정 삭제 기능이 있는 앱에 대해 Sign in with Apple 토큰 revoke를 요구한다
(TN3194 / 심사 가이드 5.1.1(v)). 미구현 시 5.1.1(v)로 반려될 수 있다.

흐름:

1. 로그인 시 `authorizationCode`를 `https://appleid.apple.com/auth/token`으로 교환 → `refresh_token` 저장
   - **매 로그인마다 갱신한다.** 사용자가 iOS 설정에서 앱 연결을 끊었다가 다시 로그인하면
     기존 토큰은 죽고 새 연결이 생기므로, 저장해둔 값을 믿으면 탈퇴 시 revoke가 조용히 실패한다.
   - 교환 실패는 로그만 남기고 기존 값을 유지한 채 로그인을 진행한다.
2. 탈퇴 시 `https://appleid.apple.com/auth/revoke` 호출 → 그 다음 `member.withdrawal()`
3. revoke 실패는 로그만 남기고 탈퇴는 정상 진행한다

client_secret은 고정값이 아니라 `.p8` 개인키로 ES256 서명한 단기 JWT(1시간)다.

## 5. 필요한 설정

Apple Developer 콘솔에서 준비:

- App ID에 **Sign in with Apple** capability 활성화
- **Keys** 메뉴에서 Sign in with Apple 용 키 생성 → `.p8` 다운로드 (재다운로드 불가)
- Team ID / Key ID 확인

`.env` (env_file로 user-server에 주입):

```
APPLE_CLIENT_ID=com.unimal.ios.stomap   # iOS Bundle ID = identityToken의 aud
APPLE_TEAM_ID=
APPLE_KEY_ID=
APPLE_PRIVATE_KEY=                       # .p8 내용. 개행은 \n 리터럴로 넣어도 된다
```

`APPLE_TEAM_ID` / `APPLE_KEY_ID` / `APPLE_PRIVATE_KEY`가 비어 있으면
토큰 교환과 revoke는 WARN 로그만 남기고 동작하지 않는다. **심사 전 반드시 채워야 한다.**

## 6. 운영 시 주의

- **Private Relay 이메일**: 사용자가 이메일 가리기를 선택하면
  `xxxx@privaterelay.appleid.com`으로 온다. 이 주소로 메일을 보내려면
  Apple Developer 콘솔에 발신 도메인을 등록해야 도착한다.
- **이름은 최초 1회만**: 테스트 중 재로그인하면 `name`이 오지 않는다.
  기기에서 "Apple ID > 암호 및 보안 > Apple로 로그인" 에서 앱 연결을 해제해야 초기화된다.
- 애플 공개키(JWKS)는 kid 기준 캐시. 모르는 kid가 오면 재조회하되 5분 간격으로 스로틀링한다.

## 7. 변경 파일

```
user/service/login/apple/AppleProperties.kt              (신규)
user/service/login/apple/AppleIdentityTokenVerifier.kt   (신규)
user/service/login/apple/AppleAuthClient.kt              (신규)
user/service/login/apple/dto/AppleDto.kt                 (신규)
user/service/login/AppleLoginObject.kt                   (신규)
user/service/login/LoginService.kt                       (수정) apple 분기 + 탈퇴 revoke
user/controller/LoginController.kt                       (수정) 엔드포인트 추가
user/controller/request/LoginRequest.kt                  (수정) AppleLoginRequest
user/domain/member/Member.kt                             (수정) providerId, appleRefreshToken
user/domain/member/MemberRepository.kt                   (수정) findByProviderAndProviderId
user/service/login/dto/UserInfo.kt                       (수정) providerId
user/service/login/enums/LoginType.kt                    (수정) APPLE
web-common/exception/ErrorCode.kt                        (수정) APPLE_* 에러코드
user/resources/application.yml, application-docker.yml   (수정) custom.apple.*
```

DB는 `ddl-auto: update`로 `member.provider_id`, `member.apple_refresh_token` 컬럼이 자동 추가된다.
