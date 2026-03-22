package com.shan.chat.adapter.in.web.common.config;

import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class H2ConsoleRedirectConfig implements WebMvcConfigurer {

    private static final String H2_CONSOLE_PATH = "/h2-console";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // '/h2-console' 요청을 콘솔 서블릿 매핑 경로로 보정한다.
        registry.addRedirectViewController(H2_CONSOLE_PATH, H2_CONSOLE_PATH + "/");
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true")
    @ConditionalOnClass(name = "org.h2.server.web.JakartaWebServlet")
    @ConditionalOnMissingBean(name = "h2Console")
    public ServletRegistrationBean<Servlet> customH2ConsoleServletRegistration() {
        Servlet servlet = instantiateH2ConsoleServlet();
        return new ServletRegistrationBean<>(servlet, H2_CONSOLE_PATH + "/*");
    }

    private Servlet instantiateH2ConsoleServlet() {
        try {
            Class<?> clazz = Class.forName("org.h2.server.web.JakartaWebServlet");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof Servlet servlet) {
                return servlet;
            }
            throw new IllegalStateException("H2 콘솔 서블릿 타입이 올바르지 않습니다.");
        } catch (Exception e) {
            throw new IllegalStateException("H2 콘솔 서블릿 생성에 실패했습니다. h2 의존성을 확인하세요.", e);
        }
    }
}
