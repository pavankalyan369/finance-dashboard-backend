package com.backend.security;

import com.backend.entity.AccountStatus;
import com.backend.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. Skip auth endpoints
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // 2. No token → continue
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            // Invalid token → ignore
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Authenticate if not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                var userDetails = userDetailsService.loadUserByUsername(email);

                // FINAL SECURITY BLOCK (THIS IS YOUR CODE)
                if (jwtService.isValid(token, userDetails)) {

                    CustomUserDetails customUser = (CustomUserDetails) userDetails;
                    User user = customUser.getUser();

                    //  BLOCK PERMANENTLY DELETED USERS
                    if (user.isDeleted()) {
                        filterChain.doFilter(request, response);
                        return;
                    }


                    //  BLOCK INACTIVE / DELETED USERS
                    if (user.getStatus() != AccountStatus.ACTIVE) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    //  INVALIDATE OLD TOKENS (FORCE LOGOUT)
                    Integer tokenVersion = jwtService.extractTokenVersion(token);
                    if (!tokenVersion.equals(user.getTokenVersion())) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    //  SET AUTHENTICATION
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception ex) {
                //  Do nothing → request continues safely
            }
        }

        filterChain.doFilter(request, response);
    }
}