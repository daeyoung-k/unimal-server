package com.unimal.user.service.authentication

import com.unimal.user.grpc.authentication.AuthCodeGrpcRequest
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authCodeGrpcRequest: AuthCodeGrpcRequest
) {

    fun sendMailAuthRequest(email: String) {
        authCodeGrpcRequest.sendMailAuthRequest(email)
    }

    fun sendTelAuthRequest(email: String, tel: String) {
        authCodeGrpcRequest.sendTelAuthRequest(email, tel)
    }
}