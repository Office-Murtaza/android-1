package com.batm.security;

import com.batm.service.UserService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class JWTSecurityAdapter extends WebSecurityConfigurerAdapter {

    @Value("${security.enabled}")
    private Boolean securityEnabled;

    @Autowired
    private JWTEntryPoint entryPoint;

    @Autowired
    private JWTTokenFilter tokenFilter;

    @Autowired
    private UserService userService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
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
    public void configure(HttpSecurity http) throws Exception {
        if (securityEnabled) {
            http.cors().and().csrf().disable()
                    .exceptionHandling().authenticationEntryPoint(entryPoint).and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeRequests()
                    .antMatchers("/api/v1/check").permitAll()
                    .antMatchers("/api/v1/verify").permitAll()
                    .antMatchers("/api/v1/register").permitAll()
                    .antMatchers("/api/v1/recover").permitAll()
                    .antMatchers("/api/v1/refresh").permitAll()
                    .antMatchers("/api/v1/test/**").permitAll()
                    .antMatchers("/api/v1/wallet/**").permitAll()
                    .antMatchers("/api/v1/ws/**").permitAll()
                    .anyRequest().authenticated();

            http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            http.cors().and().csrf().disable()
                    .exceptionHandling().authenticationEntryPoint(entryPoint).and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeRequests()
                    .anyRequest().permitAll();

            http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }
}