package com.unimal.user.service.authentication

import com.unimal.user.controller.request.*
import com.unimal.user.grpc.authentication.AuthCodeGrpcRequest
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authCodeGrpcRequest: AuthCodeGrpcRequest,
    private val authenticationManager: AuthenticationManager
) {

    fun sendMailAuthCodeRequest(emailRequest: EmailRequest) {
        val key = "${emailRequest.email}:auth-code"
        authCodeGrpcRequest.sendMailAuthRequest(key, emailRequest.email)
    }

    fun emailAuthCodeVerify(emailAuthCodeVerifyRequest: EmailAuthCodeVerifyRequest) {
        val key = "${emailAuthCodeVerifyRequest.email}:auth-code"
        val authCode = authenticationManager.getAuthCode(key)

        authenticationManager.authCodeVerify(authCode, emailAuthCodeVerifyRequest.code)
        authenticationManager.setAuthCodeSuccess(key)
    }

    fun sendTelAuthCodeRequest(telRequest: TelRequest) {
        val key = "${telRequest.tel}:auth-code"
        authCodeGrpcRequest.sendTelAuthRequest(key, telRequest.tel)
    }

    fun telAuthCodeVerify(telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest) {
        val key = "${telAuthCodeVerifyRequest.tel}:auth-code"
        val authCode = authenticationManager.getAuthCode(key)

        authenticationManager.authCodeVerify(authCode, telAuthCodeVerifyRequest.code)
        authenticationManager.setAuthCodeSuccess(key)
    }

    fun sendEmailTelAuthCodeRequest(emailTelAuthCodeRequest: EmailTelAuthCodeRequest) {
        val key = "${emailTelAuthCodeRequest.email}:${emailTelAuthCodeRequest.tel}:auth-code"
        authCodeGrpcRequest.sendTelAuthRequest(key, emailTelAuthCodeRequest.tel)
    }

    fun emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest) {
        val key = "${emailTelAuthCodeVerifyRequest.email}:${emailTelAuthCodeVerifyRequest.tel}:auth-code"
        val authCode = authenticationManager.getAuthCode(key)

        authenticationManager.authCodeVerify(authCode, emailTelAuthCodeVerifyRequest.code)
        authenticationManager.setAuthCodeSuccess(key)
    }

}