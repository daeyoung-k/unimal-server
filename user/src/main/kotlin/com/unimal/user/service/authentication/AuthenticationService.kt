package com.unimal.user.service.authentication

import com.unimal.user.controller.request.EmailRequest
import com.unimal.user.controller.request.EmailAuthCodeVerifyRequest
import com.unimal.user.controller.request.TelAuthCodeRequest
import com.unimal.user.controller.request.TelAuthCodeVerifyRequest
import com.unimal.user.grpc.authentication.AuthCodeGrpcRequest
import com.unimal.webcommon.exception.AuthCodeException
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authCodeGrpcRequest: AuthCodeGrpcRequest,
    private val authenticationObject: AuthenticationObject
) {

    fun sendMailAuthCodeRequest(emailRequest: EmailRequest) {
        authCodeGrpcRequest.sendMailAuthRequest(emailRequest.email)
    }

    fun emailAuthCodeVerify(emailAuthCodeVerifyRequest: EmailAuthCodeVerifyRequest) {
        val key = "${emailAuthCodeVerifyRequest.email}:auth-code"
        val authCode = authenticationObject.getAuthCode(key)

        if (authCode != emailAuthCodeVerifyRequest.code) {
            throw AuthCodeException("잘못된 인증코드입니다.")
        } else {
            authenticationObject.setAuthCodeSuccess(key)
        }
    }

    fun sendTelAuthCodeRequest(telAuthCodeRequest: TelAuthCodeRequest) {
        authCodeGrpcRequest.sendTelAuthRequest(telAuthCodeRequest.email, telAuthCodeRequest.tel)
    }

    fun telAuthCodeVerify(telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest) {
        val key = "${telAuthCodeVerifyRequest.email}:${telAuthCodeVerifyRequest.tel}:auth-code"
        val authCode = authenticationObject.getAuthCode(key)

        if (authCode != telAuthCodeVerifyRequest.code) {
            throw AuthCodeException("잘못된 인증코드입니다.")
        } else {
            authenticationObject.setAuthCodeSuccess(key)
        }

    }
}