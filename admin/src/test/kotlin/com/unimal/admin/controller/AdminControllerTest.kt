package com.unimal.admin.controller

import kotlin.test.Test
import kotlin.test.assertEquals

class AdminControllerTest {

    @Test
    fun `root redirects to app member list`() {
        val adminController = AdminController()

        val viewName = adminController.index()

        assertEquals("redirect:/members", viewName)
    }
}
