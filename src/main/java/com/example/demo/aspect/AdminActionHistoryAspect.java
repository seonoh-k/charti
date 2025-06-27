package com.example.demo.aspect;

import com.example.demo.annotation.AdminActionHistoryAuditLog;
import com.example.demo.entity.AdminActionHistory;
import com.example.demo.service.AdminActionHistoryService;
import com.example.demo.dto.AdminDTO;
import com.example.demo.users.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminActionHistoryAspect {

    private final AdminActionHistoryService historyService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(adminActionHistoryAuditLog)")
    public void pointcut(AdminActionHistoryAuditLog adminActionHistoryAuditLog) {}

    @Around("pointcut(adminActionHistoryAuditLog)")
    public Object around(ProceedingJoinPoint pjp,
                         AdminActionHistoryAuditLog adminActionHistoryAuditLog) throws Throwable {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs.getRequest();

        AdminDTO loginAdmin = authService.getLoginAdmin();

        // HTTP 메서드 정보
        String httpMethod = req.getMethod();

        // JSON 바디 파라미터 직렬화
        String jsonParams = null;
        for (Object arg : pjp.getArgs()) {
            if (       arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof org.springframework.ui.Model
                    || arg instanceof org.springframework.validation.BindingResult
                    || arg instanceof org.springframework.data.domain.Pageable
                    || arg instanceof org.springframework.web.servlet.mvc.support.RedirectAttributes) {
                continue;
            }
            try {
                jsonParams = objectMapper.writeValueAsString(arg);
            } catch (JsonProcessingException e) {
                jsonParams = "JSON_SERIALIZE_ERROR";
            }
            break;
        }
        String params = jsonParams != null ? jsonParams : req.getQueryString();

        AdminActionHistory.AdminActionHistoryBuilder builder = AdminActionHistory.builder()
                .adminId(loginAdmin.getId())
                .adminName(loginAdmin.getName())
                .category(adminActionHistoryAuditLog.category().name())
                .httpMethod(httpMethod)  // HTTP 메서드 추가
                .path(req.getRequestURI())
                .params(params)
                .requestDate(LocalDateTime.now());

        Object result;
        try {
            result = pjp.proceed();

            if (result instanceof ResponseEntity<?>) {
                ResponseEntity<?> resp = (ResponseEntity<?>) result;
                builder.status(resp.getStatusCodeValue());
                if (resp.getBody() != null) {
                    String json = objectMapper.writeValueAsString(resp.getBody());
                    builder.response(json);
                } else {
                    builder.response(null);
                }
            } else {
                builder.status(200)
                        .response(result != null ? result.toString() : null);
            }
        } catch (Throwable ex) {
            builder.status(500)
                    .response(ex.getMessage());
            throw ex;
        } finally {
            builder.responseDate(LocalDateTime.now());
            historyService.saveLog(builder.build());
        }

        return result;
    }
}
