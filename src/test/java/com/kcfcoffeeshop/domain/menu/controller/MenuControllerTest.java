package com.kcfcoffeeshop.domain.menu.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    private static RedisServer redisServer;

    static {
        try {
            redisServer = new RedisServer(6380);
            redisServer.start();
        } catch (Exception e) {
            // 이미 실행 중이면 무시
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("메뉴 목록 조회 성공")
    void getMenuList_success() throws Exception {
        mockMvc.perform(get("/api/menus")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("인기 메뉴 조회 성공")
    void getBestMenu_success() throws Exception {
        mockMvc.perform(get("/api/menus/best"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}