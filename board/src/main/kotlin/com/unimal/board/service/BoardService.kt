package com.unimal.board.service

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.service.files.FilesManager
import com.unimal.common.dto.CommonUserInfo
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class BoardService(
    private val filesManager: FilesManager
) {

    fun posting(
        userInfo: CommonUserInfo,
        postsCreateRequest: PostsCreateRequest,
        files: List<MultipartFile>?
    ) {

        if (files?.isNotEmpty() == true) {
//            filesManager.multipleUploadFile(files)
            print(filesManager.uploadFile(files[0]))
        }

    }


}