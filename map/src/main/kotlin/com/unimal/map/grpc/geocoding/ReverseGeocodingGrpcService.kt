package com.unimal.map.grpc.geocoding

import com.unimal.map.service.geocoding.GeocodingObject
import com.unimal.proto.map.geocoding.ReverseGeocodingGetRequest
import com.unimal.proto.map.geocoding.ReverseGeocodingGetResponse
import com.unimal.proto.map.geocoding.ReverseGeocodingServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ReverseGeocodingGrpcService(
    private val geocodingObject: GeocodingObject
): ReverseGeocodingServiceGrpc.ReverseGeocodingServiceImplBase() {

    override fun getReverseGeocoding(
        request: ReverseGeocodingGetRequest,
        responseObserver: StreamObserver<ReverseGeocodingGetResponse>
    ) {
        val addressResult = geocodingObject.getAddress(
            request.latitude,
            request.longitude,
        )

        val response = ReverseGeocodingGetResponse.newBuilder()
            .setStreetName(addressResult.streetName)
            .setStreetNumber(addressResult.streetNumber)
            .setPostalCode(addressResult.postalCode)
            .setSiDo(addressResult.siDo)
            .setGuGun(addressResult.guGun)
            .setDong(addressResult.guGun)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}