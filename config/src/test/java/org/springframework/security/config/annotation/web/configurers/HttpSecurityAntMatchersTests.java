/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.config.annotation.web.configurers;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 *
 */
public class HttpSecurityAntMatchersTests {

	AnnotationConfigWebApplicationContext context;

	MockHttpServletRequest request;

	MockHttpServletResponse response;

	MockFilterChain chain;

	@Autowired
	FilterChainProxy springSecurityFilterChain;

	@BeforeEach
	public void setup() {
		this.request = new MockHttpServletRequest("GET", "");
		this.response = new MockHttpServletResponse();
		this.chain = new MockFilterChain();
	}

	@AfterEach
	public void cleanup() {
		if (this.context != null) {
			this.context.close();
		}
	}

	// SEC-3135
	@Test
	public void antMatchersMethodAndNoPatterns() throws Exception {
		loadConfig(AntMatchersNoPatternsConfig.class);
		this.request.setMethod("POST");
		this.springSecurityFilterChain.doFilter(this.request, this.response, this.chain);
		assertThat(this.response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
	}

	// SEC-3135
	@Test
	public void antMatchersMethodAndEmptyPatterns() throws Exception {
		loadConfig(AntMatchersEmptyPatternsConfig.class);
		this.request.setMethod("POST");
		this.springSecurityFilterChain.doFilter(this.request, this.response, this.chain);
		assertThat(this.response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
	}

	public void loadConfig(Class<?>... configs) {
		this.context = new AnnotationConfigWebApplicationContext();
		this.context.register(configs);
		this.context.refresh();
		this.context.getAutowireCapableBeanFactory().autowireBean(this);
	}

	@EnableWebSecurity
	@Configuration
	static class AntMatchersNoPatternsConfig {

		@Bean
		SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.requestMatchers()
					.antMatchers(HttpMethod.POST)
					.and()
				.authorizeRequests()
					.anyRequest().denyAll();
			// @formatter:on
			return http.build();
		}

		@Bean
		UserDetailsService userDetailsService() {
			return new InMemoryUserDetailsManager();
		}

	}

	@EnableWebSecurity
	@Configuration
	static class AntMatchersEmptyPatternsConfig {

		@Bean
		SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.requestMatchers()
					.antMatchers("/never/")
					.antMatchers(HttpMethod.POST, new String[0])
					.and()
				.authorizeRequests()
					.anyRequest().denyAll();
			// @formatter:on
			return http.build();
		}

		@Bean
		UserDetailsService userDetailsService() {
			return new InMemoryUserDetailsManager();
		}

	}

}
