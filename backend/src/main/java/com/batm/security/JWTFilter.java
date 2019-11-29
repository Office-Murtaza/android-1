package com.batm.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.batm.util.Constant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import com.batm.model.AccessDenied;
import com.batm.entity.Token;
import com.batm.repository.TokenRep;
import com.fasterxml.jackson.databind.ObjectMapper;

@AllArgsConstructor
public class JWTFilter extends GenericFilterBean {

    private TokenProvider tokenProvider;
    private TokenRep tokenRep;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);

        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
            Token token = tokenRep.findByAccessToken(jwt);

            if (token == null) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setStatus(HttpStatus.FORBIDDEN.value());
                ObjectMapper mapper = new ObjectMapper();
                response.getWriter().write(mapper.writeValueAsString(new AccessDenied(403, "Invalid access token")));
                return;
            }

            Authentication authentication = this.tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constant.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}