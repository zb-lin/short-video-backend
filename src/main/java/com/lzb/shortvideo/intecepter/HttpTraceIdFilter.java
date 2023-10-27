package com.lzb.shortvideo.intecepter;

import com.lzb.shortvideo.constant.MDCKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.UUID;

/**
 * 设置链路追踪的值，初期单体项目先简单用
 */
@Slf4j
@WebFilter(urlPatterns = "/*")
@Component
public class HttpTraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String tid = UUID.randomUUID().toString();
        MDC.put(MDCKey.TID, tid);
        chain.doFilter(request, response);
    }
}
