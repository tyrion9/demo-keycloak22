package com.example.demokeycloak22.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class AuditFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MDC.put("traceId", RandomStringUtils.randomAlphanumeric(8));
        MDC.put("identity", request.getUserPrincipal().getName());
        MDC.put("path", request.getServletPath());
        MDC.put("clientIp", getClientIp(request));
        MDC.put("method", request.getMethod());

        Map mapAuditHeader = auditHeader(request,
                "User-Agent", "Device-Id");

        CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(request);
        CustomHttpServletResponseWrapper wrappedResponse = new CustomHttpServletResponseWrapper(response);


        log.info("ALOG_SRV_REQ, request={}, header={}",
                wrappedRequest.getBody().replaceAll("[\\n|\\r]", ""),
                mapAuditHeader);

        // Continue the filter chain
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String msgResp = wrappedResponse.getCaptureAsString();

            MDC.put("httpCode", String.valueOf(wrappedResponse.getStatus()));
            MDC.put("status", getResponseResult(wrappedResponse));

            log.info("ALOG_SRV_RES, request={}, response={}, header={}",
                    wrappedRequest.getBody().replaceAll("[\\n|\\r]", ""),
                    msgResp != null && msgResp.length() <= 200 ? msgResp : Objects.requireNonNull(msgResp).substring(0, 199),
                    mapAuditHeader);
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            // The first IP address in the list is the original client IP
            return ipAddress.split(",")[0];
        }

        return request.getRemoteAddr();
    }

    public static String getResponseResult(CustomHttpServletResponseWrapper response) throws IOException {
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            if (response.getCaptureAsString().contains("\"Error\" = true"))
                return "FAIL";

            return "SUCC";
        }

        return "FAIL";
    }

    public static Map<String, String> auditHeader(HttpServletRequest req, String... keys) {
        Map<String, String> auditHeader = new HashMap<>();

        if (keys == null || keys.length == 0)
            return auditHeader;

        for (String key : keys) {
            auditHeader.put(key, req.getHeader(key));
        }

        return auditHeader;
    }

}