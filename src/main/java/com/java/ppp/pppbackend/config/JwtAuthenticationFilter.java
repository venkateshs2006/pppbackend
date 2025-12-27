package com.java.ppp.pppbackend.config;
import com.java.ppp.pppbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Ensure you have this or use System.out
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j // Use Lombok logger or System.out.println
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Check if header exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JwtFilter: No Bearer token found in request to " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extract Token
            jwt = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JwtFilter: Extracted Username: " + username);

            // 3. Authenticate
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 4. Validate Token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtFilter: Authentication Successful for " + username);
                } else {
                    System.out.println("JwtFilter: Token validation failed for " + username);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JwtFilter Error: Token Expired: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("JwtFilter Error: Invalid Token Format: " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("JwtFilter Error: Invalid Signature: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("JwtFilter Error: Generic Exception: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}