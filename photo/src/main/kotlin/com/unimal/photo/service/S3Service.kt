package com.unimal.photo.service

import com.unimal.photo.service.s3.S3Manager
import com.unimal.photo.service.s3.UploadType
import com.unimal.photo.service.s3.dto.MultipleFiles
import com.unimal.photo.service.s3.dto.UploadFileResult
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.collections.map

@Service
class S3Service(
    private val s3Manager: S3Manager,
) {

    fun uploadFile(file: MultipartFile): UploadFileResult {
        val originalFilename = file.originalFilename ?: "unnamed"
        val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

        val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
        val fileType = UploadType.from(getType.type)

        val key = fileType.path + encodedFilename + "." + getType.subType
        s3Manager.uploadFile(key, file)

        return UploadFileResult(
            originalFilename = originalFilename,
            key = key
        )
    }

    /**
     * 멀티 파일 업로드 - S3TransferManager 사용
     * MultipartFile → 임시 파일 생성 → 업로드 → 임시 파일 삭제
     *
     * 왜 tmp(Path) 사용?
     * Transfer Manager의 최적 경로가 “파일(Path)” 기준
     * MultipartFile의 내부는 대개 요청(InputStream) 에 의존. 컨트롤러가 리턴되고 요청이 끝나면 컨테이너가 스트림을 닫거나 청소할 수 있음.
     * Transfer Manager 업로드는 비동기라 메서드 리턴 후에도 전송이 계속될 수 있는데, 스트림이 사라지면 업로드가 깨짐.
     * 그래서 디스크에 안전하게 복사해서, 업로드가 끝날 때까지 데이터가 안정적으로 존재하도록 함.
     *
     * 대용량/다중 파일에서 메모리 폭주 방지
     * 10장 동시 업로드를 모두 메모리에 올려두면 힙을 크게 잡아먹고 GC 압박이 커짐.
     * 임시 파일로 내려두면 메모리 사용량을 일정하게 유지하면서 병렬 업로드 가능.
     */
    fun multiUploadFile(files: List<MultipartFile>): List<UploadFileResult> {

        val multipleTmpFiles = files.map { file ->
            val originalFilename = file.originalFilename ?: "unnamed"
            val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

            val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
            val fileType = UploadType.from(getType.type)

            val key = fileType.path + encodedFilename + "." + getType.subType

            val tmp = Files.createTempFile("unimal-", ".${getType.subType }")
            file.inputStream.use { Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING) }
            MultipleFiles(originalFilename, key, tmp)
        }

        return s3Manager.multiUploadFile(multipleTmpFiles)
    }
}