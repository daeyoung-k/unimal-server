# Signup V2 API & AuthController 분리 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `/auth/signup/manual/v2` 신규 엔드포인트를 추가하고, `AuthController`를 `SignUpController` / `LoginController` / `AuthenticationController` 3개로 분리한다.

**Architecture:**
v2 signup은 계정 생성 후 소셜 로그인 tel-missing 플로우와 동일하게 `TelNotFoundException(code=1009, data=email)`을 throw하여, Flutter가 기존 소셜 로그인 tel 화면으로 라우팅한다. tel 등록은 기존 `/auth/email-tel/code-request` + `/auth/tel/check-update` 엔드포인트를 그대로 재활용한다.

**Tech Stack:** Kotlin, Spring Boot 3, JUnit 5, MockK 1.14.2, Spring Data Redis

---

## 파일 맵

| 작업 | 파일 |
|------|------|
| 신규 생성 | `user/src/main/kotlin/com/unimal/user/controller/request/SignupV2Request.kt` |
| 수정 | `user/src/main/kotlin/com/unimal/user/service/login/ManualLoginObject.kt` |
| 수정 | `user/src/main/kotlin/com/unimal/user/service/login/LoginService.kt` |
| 신규 생성 | `user/src/main/kotlin/com/unimal/user/controller/SignUpController.kt` |
| 신규 생성 | `user/src/main/kotlin/com/unimal/user/controller/LoginController.kt` |
| 신규 생성 | `user/src/main/kotlin/com/unimal/user/controller/AuthenticationController.kt` |
| 삭제 | `user/src/main/kotlin/com/unimal/user/controller/AuthController.kt` |
| 신규 생성 (테스트) | `user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt` |

---

### Task 1: SignupV2Request DTO 생성

**Files:**
- Create: `user/src/main/kotlin/com/unimal/user/controller/request/SignupV2Request.kt`

- [ ] **Step 1: DTO 파일 생성**

```kotlin
package com.unimal.user.controller.request

import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

data class SignupV2Request(
    @field:NotBlank
    val nickname: String,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val checkPassword: String,
    @field:NotBlank
    val password: String,
) {
    fun toUserInfo() = UserInfo(
        email = email,
        password = BCryptPasswordEncoder().encode(password.lowercase()),
        nickname = nickname,
        provider = LoginType.MANUAL.name
    )
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :user:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/controller/request/SignupV2Request.kt
git commit -m "[feat] SignupV2Request DTO 추가"
```

---

### Task 2: ManualLoginObject에 emailSuccessCheck 추가

**Files:**
- Modify: `user/src/main/kotlin/com/unimal/user/service/login/ManualLoginObject.kt`
- Test: `user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt`

현재 `emailTelSuccessCheck`는 email + tel 둘 다 검사한다. v2 signup은 이메일만 검사해야 하므로 `emailSuccessCheck`를 추가한다.

- [ ] **Step 1: 실패하는 테스트 작성**

파일 생성: `user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt`

```kotlin
package com.unimal.user.service.login

import com.unimal.user.utils.RedisCacheManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ManualLoginObjectEmailCheckTest {

    private val redisCacheManager: RedisCacheManager = mockk()
    private val memberObject: com.unimal.user.service.member.MemberObject = mockk()
    private val memberRepository: com.unimal.user.domain.member.MemberRepository = mockk()

    private val manualLoginObject = ManualLoginObject(
        memberObject = memberObject,
        redisCacheManager = redisCacheManager,
        memberRepository = memberRepository
    )

    @Test
    fun `이메일 인증 완료된 경우 true 반환`() {
        every { redisCacheManager.getCache("test@test.com:auth-code") } returns "SUCCESS"

        assertTrue(manualLoginObject.emailSuccessCheck("test@test.com"))
    }

    @Test
    fun `이메일 인증 미완료 시 false 반환`() {
        every { redisCacheManager.getCache("test@test.com:auth-code") } returns null

        assertFalse(manualLoginObject.emailSuccessCheck("test@test.com"))
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew :user:test --tests "com.unimal.user.service.login.ManualLoginObjectEmailCheckTest" 2>&1 | tail -20
```
Expected: FAILED — `emailSuccessCheck` 메서드 없음

- [ ] **Step 3: ManualLoginObject에 메서드 추가**

`ManualLoginObject.kt`에 기존 `emailTelSuccessCheck` 아래에 추가:

```kotlin
fun emailSuccessCheck(email: String): Boolean {
    val emailKey = "$email:auth-code"
    return redisCacheManager.getCache(emailKey) == "SUCCESS"
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew :user:test --tests "com.unimal.user.service.login.ManualLoginObjectEmailCheckTest"
```
Expected: BUILD SUCCESSFUL, 2 tests passed

- [ ] **Step 5: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/service/login/ManualLoginObject.kt \
        user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt
git commit -m "[feat] ManualLoginObject에 emailSuccessCheck 추가"
```

---

### Task 3: LoginService.signupV2() 추가

**Files:**
- Modify: `user/src/main/kotlin/com/unimal/user/service/login/LoginService.kt`
- Test: `user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt` (기존 파일에 추가)

- [ ] **Step 1: 실패하는 테스트 작성**

`SignupV2Test.kt` 아래에 다음 클래스를 **추가** (기존 ManualLoginObjectEmailCheckTest는 그대로 유지):

```kotlin
class LoginServiceSignupV2Test {

    private val kakaoLoginObject: LoginInterface = mockk()
    private val naverLoginObject: LoginInterface = mockk()
    private val googleLoginObject: LoginInterface = mockk()
    private val manualLoginObject: ManualLoginObject = mockk()
    private val tokenManager: com.unimal.user.service.token.TokenManager = mockk()
    private val memberObject: com.unimal.user.service.member.MemberObject = mockk()
    private val memberKafkaTopic: com.unimal.user.kafka.topics.MemberKafkaTopic = mockk()
    private val memberRepository: com.unimal.user.domain.member.MemberRepository = mockk()

    private val loginService = LoginService(
        kakaoLoginObject = kakaoLoginObject,
        naverLoginObject = naverLoginObject,
        googleLoginObject = googleLoginObject,
        manualLoginObject = manualLoginObject,
        tokenManager = tokenManager,
        memberObject = memberObject,
        memberKafkaTopic = memberKafkaTopic,
        memberRepository = memberRepository,
    )

    private val validRequest = com.unimal.user.controller.request.SignupV2Request(
        nickname = "테스터",
        email = "test@test.com",
        password = "Test1234!",
        checkPassword = "Test1234!",
    )

    @Test
    fun `이메일 중복 시 DuplicatedException 발생`() {
        every { memberRepository.findByEmail("test@test.com") } returns mockk()

        org.junit.jupiter.api.assertThrows<com.unimal.webcommon.exception.DuplicatedException> {
            loginService.signupV2(validRequest)
        }
    }

    @Test
    fun `비밀번호 불일치 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null

        val request = validRequest.copy(checkPassword = "WrongPass1!")
        org.junit.jupiter.api.assertThrows<com.unimal.webcommon.exception.LoginException> {
            loginService.signupV2(request)
        }
    }

    @Test
    fun `비밀번호 형식 불일치 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns false

        val request = validRequest.copy(password = "simple", checkPassword = "simple")
        org.junit.jupiter.api.assertThrows<com.unimal.webcommon.exception.LoginException> {
            loginService.signupV2(request)
        }
    }

    @Test
    fun `이메일 인증 미완료 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns true
        every { manualLoginObject.emailSuccessCheck("test@test.com") } returns false

        org.junit.jupiter.api.assertThrows<com.unimal.webcommon.exception.LoginException> {
            loginService.signupV2(validRequest)
        }
    }

    @Test
    fun `정상 가입 시 TelNotFoundException 발생 (code=1009, data=email)`() {
        val savedMember: com.unimal.user.domain.member.Member = mockk {
            every { email } returns "test@test.com"
        }
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns true
        every { manualLoginObject.emailSuccessCheck("test@test.com") } returns true
        every { memberObject.signIn(any()) } returns savedMember

        val ex = org.junit.jupiter.api.assertThrows<com.unimal.webcommon.exception.TelNotFoundException> {
            loginService.signupV2(validRequest)
        }
        org.junit.jupiter.api.Assertions.assertEquals("test@test.com", ex.data)
        org.junit.jupiter.api.Assertions.assertEquals(1009, ex.code)
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew :user:test --tests "com.unimal.user.service.login.LoginServiceSignupV2Test" 2>&1 | tail -20
```
Expected: FAILED — `signupV2` 메서드 없음

- [ ] **Step 3: LoginService에 signupV2 구현**

`LoginService.kt`의 `signup()` 함수 바로 아래에 추가:

```kotlin
@Transactional
fun signupV2(signupRequest: SignupV2Request): Nothing {
    memberRepository.findByEmail(signupRequest.email)
        ?.let { throw DuplicatedException(ErrorCode.EMAIL_USED.message) }

    if (signupRequest.password.lowercase() != signupRequest.checkPassword.lowercase()) {
        throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
    }

    if (!memberObject.passwordFormatCheck(signupRequest.password.lowercase())) {
        throw LoginException(ErrorCode.PASSWORD_FORMAT_INVALID.message)
    }

    manualLoginObject as ManualLoginObject
    if (!manualLoginObject.emailSuccessCheck(signupRequest.email)) {
        throw LoginException(ErrorCode.AUTHENTICATION_NOT_COMPLETED.message)
    }

    val member = memberObject.signIn(signupRequest.toUserInfo())

    // 소셜 로그인 tel-missing 플로우와 동일: Flutter가 code=1009 감지 후 tel 입력 화면으로 이동
    throw TelNotFoundException(data = member.email)
}
```

`LoginService.kt` import 맨 위에 없으면 추가:
```kotlin
import com.unimal.user.controller.request.SignupV2Request
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew :user:test --tests "com.unimal.user.service.login.LoginServiceSignupV2Test"
```
Expected: BUILD SUCCESSFUL, 5 tests passed

- [ ] **Step 5: 전체 user 테스트 확인 (회귀 없음)**

```bash
./gradlew :user:test
```
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

- [ ] **Step 6: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/service/login/LoginService.kt \
        user/src/test/kotlin/com/unimal/user/service/login/SignupV2Test.kt
git commit -m "[feat] LoginService.signupV2 구현 — tel-missing 플로우 재활용"
```

---

### Task 4: SignUpController 생성

**Files:**
- Create: `user/src/main/kotlin/com/unimal/user/controller/SignUpController.kt`

`AuthController`에서 signup 관련 엔드포인트를 이동하고, v2 엔드포인트를 추가한다.

- [ ] **Step 1: SignUpController 생성**

```kotlin
package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.controller.request.SignupRequest
import com.unimal.user.controller.request.SignupV2Request
import com.unimal.user.service.login.LoginService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SignUpController(
    private val loginService: LoginService
) {
    @PostMapping("/auth/signup/manual")
    fun manualSignup(
        @RequestBody @Valid signupRequest: SignupRequest,
    ): CommonResponse {
        loginService.signup(signupRequest)
        return CommonResponse()
    }

    @PostMapping("/v2/auth/signup/manual")
    fun manualSignupV2(
        @RequestBody @Valid signupRequest: SignupV2Request,
    ): CommonResponse {
        loginService.signupV2(signupRequest)
    }
}
```

> **Note:** `signupV2`는 항상 `TelNotFoundException`을 throw하므로 `CommonResponse` 반환 라인은 실행되지 않는다. 반환 타입은 `CommonResponse`이지만 실제 응답은 `WebExceptionHandler`가 처리한다. Kotlin 컴파일러가 `Nothing` return type을 추론해 `return` 없이도 컴파일된다.

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :user:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/controller/SignUpController.kt
git commit -m "[feat] SignUpController 생성 — signup v1/v2 엔드포인트"
```

---

### Task 5: LoginController 생성

**Files:**
- Create: `user/src/main/kotlin/com/unimal/user/controller/LoginController.kt`

로그인, 토큰 재발급, 로그아웃, 탈퇴, tel 업데이트 엔드포인트를 `AuthController`에서 이동한다.

- [ ] **Step 1: LoginController 생성**

```kotlin
package com.unimal.user.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.controller.request.EmailTelAuthCodeVerifyRequest
import com.unimal.user.controller.request.GoogleLoginRequest
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.service.authentication.AuthenticationService
import com.unimal.user.service.login.LoginService
import com.unimal.user.service.token.TokenService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class LoginController(
    private val loginService: LoginService,
    private val tokenService: TokenService,
    private val authenticationService: AuthenticationService,
) {
    @GetMapping("/login/mobile/kakao")
    fun mobileKakao(
        @SocialLoginToken token: String,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(KakaoLoginRequest(token = token))
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/mobile/naver")
    fun mobileNaver(
        @RequestBody @Valid naverLoginRequest: NaverLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(naverLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/mobile/google")
    fun mobileGoogle(
        @RequestBody @Valid googleLoginRequest: GoogleLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(googleLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/manual")
    fun manualLogin(
        @RequestBody @Valid manualLoginRequest: ManualLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(manualLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/token-reissue")
    fun tokenReissue(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = tokenService.accessTokenCreate(commonUserInfo)
        response.setHeader("X-Unimal-Email", jwtToken.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/logout")
    fun logout(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        loginService.logout(commonUserInfo)
        return CommonResponse()
    }

    @GetMapping("/withdrawal")
    fun withdrawal(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        loginService.withdrawal(commonUserInfo)
        return CommonResponse()
    }

    @PostMapping("/tel/check-update")
    fun telCheckUpdate(
        @RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest,
        response: HttpServletResponse
    ): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        val jwtToken = loginService.telCheckUpdate(
            emailTelAuthCodeVerifyRequest.email,
            emailTelAuthCodeVerifyRequest.tel
        )
        response.setHeader("X-Unimal-Email", jwtToken.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        response.setHeader("X-Unimal-Provider", jwtToken.provider)
        return CommonResponse()
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :user:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/controller/LoginController.kt
git commit -m "[feat] LoginController 생성 — 로그인/토큰/로그아웃 엔드포인트"
```

---

### Task 6: AuthenticationController 생성

**Files:**
- Create: `user/src/main/kotlin/com/unimal/user/controller/AuthenticationController.kt`

인증코드 발송/검증 엔드포인트를 `AuthController`에서 이동한다.

- [ ] **Step 1: AuthenticationController 생성**

```kotlin
package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.controller.request.EmailAuthCodeVerifyRequest
import com.unimal.user.controller.request.EmailRequest
import com.unimal.user.controller.request.EmailTelAuthCodeRequest
import com.unimal.user.controller.request.EmailTelAuthCodeVerifyRequest
import com.unimal.user.controller.request.TelAuthCodeVerifyRequest
import com.unimal.user.controller.request.TelRequest
import com.unimal.user.service.authentication.AuthenticationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/email/code-request")
    fun emailCodeRequest(
        @RequestBody @Valid emailRequest: EmailRequest,
    ): CommonResponse {
        authenticationService.sendMailAuthCodeRequest(emailRequest)
        return CommonResponse()
    }

    @PostMapping("/email/code-verify")
    fun emailCodeVerify(
        @RequestBody @Valid emailAuthCodeVerifyRequest: EmailAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.emailAuthCodeVerify(emailAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-request")
    fun telCodeRequest(
        @RequestBody @Valid telRequest: TelRequest
    ): CommonResponse {
        authenticationService.sendTelAuthCodeRequest(telRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-verify")
    fun telCodeVerify(
        @RequestBody @Valid telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.telAuthCodeVerify(telAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-request")
    fun emailTelCodeRequest(
        @RequestBody @Valid emailTelAuthCodeRequest: EmailTelAuthCodeRequest
    ): CommonResponse {
        authenticationService.sendEmailTelAuthCodeRequest(emailTelAuthCodeRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-verify")
    fun emailTelCodeVerify(
        @RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        return CommonResponse()
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew :user:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add user/src/main/kotlin/com/unimal/user/controller/AuthenticationController.kt
git commit -m "[feat] AuthenticationController 생성 — 이메일/전화 인증코드 엔드포인트"
```

---

### Task 7: AuthController 삭제 및 최종 검증

**Files:**
- Delete: `user/src/main/kotlin/com/unimal/user/controller/AuthController.kt`

3개의 신규 컨트롤러가 AuthController의 모든 엔드포인트를 완전히 대체했으므로 삭제한다.

- [ ] **Step 1: AuthController 삭제**

```bash
rm user/src/main/kotlin/com/unimal/user/controller/AuthController.kt
```

- [ ] **Step 2: 전체 빌드 확인**

```bash
./gradlew :user:build
```
Expected: BUILD SUCCESSFUL — 컴파일 에러 없음

- [ ] **Step 3: 전체 테스트 확인**

```bash
./gradlew :user:test
```
Expected: BUILD SUCCESSFUL, 모든 기존 테스트 통과

- [ ] **Step 4: 엔드포인트 URL 회귀 체크**

아래 경로들이 모두 올바른 컨트롤러에 매핑되었는지 확인한다.

| 경로 | 이동된 컨트롤러 |
|------|--------------|
| `POST /auth/signup/manual` | `SignUpController` |
| `POST /auth/signup/manual/v2` | `SignUpController` (신규) |
| `GET /auth/login/mobile/kakao` | `LoginController` |
| `POST /auth/login/mobile/naver` | `LoginController` |
| `POST /auth/login/mobile/google` | `LoginController` |
| `POST /auth/login/manual` | `LoginController` |
| `GET /auth/token-reissue` | `LoginController` |
| `GET /auth/logout` | `LoginController` |
| `GET /auth/withdrawal` | `LoginController` |
| `POST /auth/tel/check-update` | `LoginController` |
| `POST /auth/email/code-request` | `AuthenticationController` |
| `POST /auth/email/code-verify` | `AuthenticationController` |
| `POST /auth/tel/code-request` | `AuthenticationController` |
| `POST /auth/tel/code-verify` | `AuthenticationController` |
| `POST /auth/email-tel/code-request` | `AuthenticationController` |
| `POST /auth/email-tel/code-verify` | `AuthenticationController` |

- [ ] **Step 5: 최종 커밋**

```bash
git add -A
git commit -m "[refactor] AuthController를 SignUpController/LoginController/AuthenticationController로 분리"
```

---

## 완료 기준

- [ ] `POST /auth/signup/manual` — 구버전 동작 그대로 (tel 필수, email+tel 인증 모두 필요)
- [ ] `POST /auth/signup/manual/v2` — `TelNotFoundException(code=1009, data=email)` 응답
- [ ] v2 signup 후 `POST /auth/email-tel/code-request` + `POST /auth/tel/check-update` 정상 동작
- [ ] `AuthController.kt` 파일 없음
- [ ] 전체 테스트 통과
