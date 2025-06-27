package com.example.demo.annotation;


import com.example.demo.enums.AdminActionHistoryCategory;

import java.lang.annotation.*;

/**
 *     @AdminActionHistoryAuditLog(category = "ADM_PAGE_ACCESS")
 *     @GetMapping("/admin")
 *     public String showAdminPage() {
 *         log.info("[GET] 👨‍💼 request Admin Page");
 *         return "admin";
 *     }
 */
@Target(ElementType.METHOD) // 메서드에만 붙일 수 있도록 지정
@Retention(RetentionPolicy.RUNTIME) //런타임 시점에도 애너테이션 정보를 읽을 수 있도록
public @interface AdminActionHistoryAuditLog {
    AdminActionHistoryCategory category();
}

