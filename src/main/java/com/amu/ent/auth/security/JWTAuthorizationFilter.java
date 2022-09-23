package com.amu.ent.auth.security;

import static com.amu.ent.auth.security.SecurityConstants.TOKEN_PREFIX;

import static com.amu.ent.auth.security.SecurityConstants.HEADER_STRING;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import com.amu.ent.configuration.RsaJWT;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	public JWTAuthorizationFilter(AuthenticationManager authManager) {
	  super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
    
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)  {
    	
        String token = request.getHeader(HEADER_STRING);
        RSAPublicKey publicKey;
        if (token != null) {
            // parse the token.
        	try {
        		publicKey =  RsaJWT.readPublicKey();
        		String user = Jwts.parser()
         			.setSigningKey(publicKey)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody()
                    .getSubject();
        		if (user != null) {
        			return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        		}
        		return null;
        	} catch (ExpiredJwtException | SignatureException | UnsupportedJwtException e) {
        		return null;
        	}
        }
        return null;
    }
}