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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

        CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(request);
        CustomHttpServletResponseWrapper wrappedResponse = new CustomHttpServletResponseWrapper(response);


        log.info("ALOG_SRV_REQ, request={}, header={}",
                getRequestBody(wrappedRequest),
                auditHeader(request));

        // Continue the filter chain
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        MDC.put("httpCode", String.valueOf(wrappedResponse.getStatus()));

        log.info("ALOG_SRV_RES, request={}, response={}, header={}",
                wrappedRequest.getBody().replaceAll("[\\n|\\r]", ""),
                wrappedResponse.getCaptureAsString(),
                auditHeader(request));
    }

    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            // The X-Forwarded-For header may contain a comma-separated list of IP addresses
            // The first IP address in the list is the original client IP
            return ipAddress.split(",")[0];
        }
        // If the X-Forwarded-For header is not present, fall back to getRemoteAddr()
        return request.getRemoteAddr();
    }

    public static Map auditHeader(HttpServletRequest req) {
        Map<String, String> auditHeader = new HashMap<>();
        auditHeader.put("User-Agent", req.getHeader("User-Agent"));
        auditHeader.put("Host", req.getHeader("Host"));
        auditHeader.put("Method", req.getMethod());

        return auditHeader;
    }

    public static String getRequestBody(HttpServletRequest request) {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            // Handle exception as needed
            e.printStackTrace();
        }
        return requestBody.toString();
    }
}