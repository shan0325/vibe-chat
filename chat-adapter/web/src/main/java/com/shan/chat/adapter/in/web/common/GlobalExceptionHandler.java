package com.shan.chat.adapter.in.web.common;

import com.shan.chat.common.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * REST 요청(Ajax)에서 발생한 ChatException → 400 Bad Request + 메시지 반환
     * 일반 페이지 요청에서 발생한 ChatException → 메인 페이지로 리다이렉트
     */
    @ExceptionHandler(ChatException.class)
    public Object handleChatException(ChatException e,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        log.warn("[ChatException] uri={}, message={}", request.getRequestURI(), e.getMessage());

        // Ajax / REST 요청인 경우 JSON 에러 응답
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isApiRequest = (request.getRequestURI().startsWith("/api/"))
                || "XMLHttpRequest".equals(requestedWith)
                || (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isApiRequest) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

        // 일반 페이지 요청은 메인으로 리다이렉트
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/main";
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpectedException(Exception e,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        log.error("[UnexpectedException] uri={}", request.getRequestURI(), e);

        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isApiRequest = (request.getRequestURI().startsWith("/api/"))
                || "XMLHttpRequest".equals(requestedWith)
                || (acceptHeader != null && acceptHeader.contains("application/json"));

        if (isApiRequest) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
        }

        redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "redirect:/main";
    }

    public record ErrorResponse(String message) {}
}

