package com.kiki.blog.app.security.controller;

import com.kiki.blog.app.security.model.BlogUserDetail;
import com.kiki.blog.openapi.api.AuthenticateApi;
import com.kiki.blog.openapi.model.AuthRequest;
import com.kiki.blog.openapi.model.AuthResponse;
import com.kiki.blog.app.security.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class AuthController implements AuthenticateApi {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(
            JwtTokenService jwtTokenService,
            @Qualifier("blogUserService") UserDetailsService userDetailsService,
            AuthenticationManager authenticationManager) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ResponseEntity<AuthResponse> authenticate(@Valid AuthRequest authRequest) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new AuthResponse().token(jwtTokenService.generateToken(userDetails)).uid(((BlogUserDetail)userDetails).getUid()), HttpStatus.OK);
    }
}
