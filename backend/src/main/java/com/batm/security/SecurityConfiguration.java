package com.batm.security;

import javax.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.batm.repository.TokenRep;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.enabled}")
    private Boolean securityEnabled;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenRep tokenRep;

    public SecurityConfiguration(AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider,
                                 UserDetailsService userDetailsService, TokenRep tokenRep) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenRep = tokenRep;
    }

    @PostConstruct
    public void init() {
        try {
            authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        } catch (Exception e) {
            throw new BeanInitializationException("Security configuration failed", e);
        }
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {

            @Override
            public String encode(CharSequence charSequence) {
                return Base64.encodeBase64String(charSequence.toString().getBytes());
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return StringUtils.equals(encode(charSequence), s);
            }
        };
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        if (securityEnabled) {
            http.csrf().disable().authorizeRequests()
                    .antMatchers("/api/v1/register").permitAll()
                    .antMatchers("/api/v1/recover").permitAll()
                    .antMatchers("/api/v1/refresh").permitAll()
                    .antMatchers("/api/v1/test/**").permitAll()
                    .antMatchers("/api/v1/**").authenticated()

                    .and().exceptionHandling().and().headers().frameOptions().disable().and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().apply(securityConfigurerAdapter());
        } else {
            http.csrf().disable().authorizeRequests()
                    .antMatchers("/api/v1/register").permitAll()
                    .antMatchers("/api/v1/recover").permitAll()
                    .antMatchers("/api/v1/refresh").permitAll()
                    .antMatchers("/api/v1/test/**").permitAll()
                    .antMatchers("/api/v1/**").permitAll()

                    .and().exceptionHandling().and().headers().frameOptions().disable().and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().apply(securityConfigurerAdapter());
        }
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider, tokenRep);
    }
}