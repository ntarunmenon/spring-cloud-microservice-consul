package org.arunm.config;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
public class OrderServiceServiceSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

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

    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     */
    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) {
        final KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();

		/*
			Spring Security, when using role-based authentication, requires that role names start
			with ROLE_. For example, an administrator role must be declared in Keycloak as ROLE_ADMIN
			or similar, not simply ADMIN.

			The class org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider
			supports an optional org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
			which can be used to map roles coming from Keycloak to roles recognized by Spring Security.
			Use, for example, org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
			to insert the ROLE_ prefix and convert the role name to upper case. The class is part of
			Spring Security Core module.
		 */
        final SimpleAuthorityMapper simpleAuthorityMapper = new SimpleAuthorityMapper();
        simpleAuthorityMapper.setConvertToUpperCase(true);
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(simpleAuthorityMapper);

        auth.authenticationProvider(keycloakAuthenticationProvider);
    }
}
