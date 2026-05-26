package com.pm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_LOG_LENGTH = 2000;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 跳过文件上传接口和头像上传
        if (isMultipartEndpoint(uri)) {
            return;
        }

        // 请求参数
        String queryString = request.getQueryString();
        String requestLog = queryString != null ? queryString : "";

        // 响应内容
        String responseLog = "";
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    String body = new String(buf, response.getCharacterEncoding());
                    responseLog = truncate(body);
                } catch (Exception e) {
                    responseLog = "(无法读取响应)";
                }
            }
        }

        int status = response.getStatus();
        if (status >= 400 || ex != null) {
            log.error("[API] {} {} params={} status={} body={}", method, uri, requestLog, status, responseLog);
        } else {
            log.info("[API] {} {} params={} status={} body={}", method, uri, requestLog, status, responseLog);
        }
    }

    private boolean isMultipartEndpoint(String uri) {
        return uri.contains("/attachments")
                || uri.contains("/avatar")
                || uri.contains("/upload")
                || uri.contains("/docs/") && uri.endsWith("/versions");
    }

    private String truncate(String str) {
        if (str == null) return "";
        if (str.length() <= MAX_LOG_LENGTH) return str;
        return str.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
    }
}
