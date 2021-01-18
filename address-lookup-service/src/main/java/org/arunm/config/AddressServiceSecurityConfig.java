package org.arunm.config;

import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
public class AddressServiceSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
            .csrf()
            .disable();
        enableHealthCheck(http);
        enableSecurityForApi(http);
    }

    private void enableSecurityForApi(HttpSecurity http) {
        try {
            http.authorizeRequests()
                    .antMatchers("/api/**")
                    .authenticated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableHealthCheck(HttpSecurity http) {
        try {
            http.authorizeRequests()
                    .antMatchers("/actuator/**")
                    .permitAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
