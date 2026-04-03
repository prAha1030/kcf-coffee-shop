package com.kcfcoffeeshop.domain.menu.service;

import com.kcfcoffeeshop.common.dto.PageResponse;
import com.kcfcoffeeshop.domain.menu.dto.response.MenuListGetResponse;
import com.kcfcoffeeshop.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private static final String MENU_CACHE_PREFIX = "menu:";

    private final MenuRepository menuRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<MenuListGetResponse> getMenuList(Pageable pageable) {
        String key = MENU_CACHE_PREFIX + "page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();

        try {
            // 캐시 조회
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                // 캐시 히트
                return objectMapper.readValue(cached, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("캐시 조회 중 오류 발생 : {}", e.getMessage());
        }

        // 캐시 미스 : DB 조회
        Page<MenuListGetResponse> page = menuRepository.findALLByDeletedAtIsNull(pageable)
                .map(MenuListGetResponse::from);
        PageResponse<MenuListGetResponse> pageResponse = PageResponse.from(page);

        try {
            // 캐시 저장
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(pageResponse), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("캐시 저장 중 오류 발생 : {}", e.getMessage());
        }
        return pageResponse;
    }
}
