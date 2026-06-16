# Admin Session Login Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build admin-only Spring Security session login for the Thymeleaf admin server.

**Architecture:** Admin uses form login and `JSESSIONID`, not user JWT. `AdminUserDetailsService` loads `admin_member` by `login_id`, checks `ACTIVE` status, and exposes `ADMIN`/`SUPER_ADMIN` authorities from `admin_member_role`.

**Tech Stack:** Kotlin, Spring Boot MVC, Spring Security, Thymeleaf, Spring Data JPA, JUnit 5.

---

## File Structure

- Modify `admin/build.gradle.kts`: add Spring Security, Thymeleaf, and security test dependencies.
- Modify `admin/src/main/kotlin/com/unimal/admin/domain/adminmember/AdminMember.kt`: add nullable-safe JPA constraints and role collection helper.
- Create `admin/src/main/kotlin/com/unimal/admin/domain/adminmember/AdminMemberRepository.kt`: query by `loginId`.
- Create `admin/src/main/kotlin/com/unimal/admin/service/adminmember/AdminUserDetailsService.kt`: convert admin account to Spring Security `UserDetails`.
- Create `admin/src/main/kotlin/com/unimal/admin/config/SecurityConfig.kt`: form login, logout, authorization, password encoder.
- Create `admin/src/main/kotlin/com/unimal/admin/controller/view/LoginViewController.kt`: serve login page.
- Create `admin/src/main/resources/templates/login.html`: login form.
- Create tests under `admin/src/test/kotlin/com/unimal/admin/service/adminmember/` and `admin/src/test/kotlin/com/unimal/admin/config/`.

---

### Task 1: Admin UserDetailsService

**Files:**
- Create: `admin/src/main/kotlin/com/unimal/admin/domain/adminmember/AdminMemberRepository.kt`
- Create: `admin/src/main/kotlin/com/unimal/admin/service/adminmember/AdminUserDetailsService.kt`
- Modify: `admin/src/main/kotlin/com/unimal/admin/domain/adminmember/AdminMember.kt`
- Test: `admin/src/test/kotlin/com/unimal/admin/service/adminmember/AdminUserDetailsServiceTest.kt`

- [ ] **Step 1: Write the failing test**

Test active admin account maps to username, password, and `ROLE_*` authorities. Test inactive account throws `UsernameNotFoundException`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :admin:test --tests com.unimal.admin.service.adminmember.AdminUserDetailsServiceTest`
Expected: FAIL because repository/service classes do not exist.

- [ ] **Step 3: Write minimal implementation**

Create repository and service. Update `AdminMember` with `roles` collection and `addRole(role: AdminRole)`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :admin:test --tests com.unimal.admin.service.adminmember.AdminUserDetailsServiceTest`
Expected: PASS.

---

### Task 2: Spring Security Form Login

**Files:**
- Modify: `admin/build.gradle.kts`
- Create: `admin/src/main/kotlin/com/unimal/admin/config/SecurityConfig.kt`
- Create: `admin/src/main/kotlin/com/unimal/admin/controller/view/LoginViewController.kt`
- Create: `admin/src/main/resources/templates/login.html`
- Test: `admin/src/test/kotlin/com/unimal/admin/config/SecurityConfigTest.kt`

- [ ] **Step 1: Write the failing tests**

Test `/login` is publicly reachable, `/reports` redirects anonymous users to `/login`, and `SUPER_ADMIN` can access `/admin-members`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :admin:test --tests com.unimal.admin.config.SecurityConfigTest`
Expected: FAIL because security dependencies/config/template are missing.

- [ ] **Step 3: Write minimal implementation**

Add dependencies, security chain, password encoder, login view controller, and login template.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :admin:test --tests com.unimal.admin.config.SecurityConfigTest`
Expected: PASS.

---

### Task 3: Full Admin Verification

**Files:**
- Existing admin source and test files.

- [ ] **Step 1: Run full admin tests**

Run: `./gradlew :admin:test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Review changed files**

Run: `git status --short`
Expected: only admin login files, existing user changes, and planned untracked mockup files are present.
