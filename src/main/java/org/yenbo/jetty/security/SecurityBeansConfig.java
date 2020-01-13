package org.yenbo.jetty.security;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.yenbo.jetty.data.InMemoryUser;
import org.yenbo.jetty.data.PasswordInfo;
import org.yenbo.jetty.repo.UserRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityBeansConfig extends WebSecurityConfigurerAdapter {

	private static final String LOGIN_FORM_URL = "/oauth2/login";

	@Autowired
	private DemoUserDetailsService demoUserDetailsService;
	@Autowired
	private StupidPasswordEncoder stupidPasswordEncoder;
	
	@Bean
	public UserRepository userRepository() {
		
		InMemoryUser admin = new InMemoryUser();
		admin.setPasswordInfo(new PasswordInfo(
				InMemoryUser.PASSWORD_PREFIX, InMemoryUser.PASSWORD_POSTFIX, "password"));
		admin.setUsername(InMemoryUser.USERNAME_ADMIN);
		admin.setProperty("property to be attached for admin");
		admin.getRoles().add(InMemoryUser.ROLE_ADMIN);
		
		InMemoryUser user = new InMemoryUser();
		user.setPasswordInfo(new PasswordInfo(
				InMemoryUser.PASSWORD_PREFIX, InMemoryUser.PASSWORD_POSTFIX, "password"));
		user.setUsername(InMemoryUser.USERNAME_USER);
		user.setProperty("property to be attached for user");
		user.getRoles().add(InMemoryUser.ROLE_USER);
		
		UserRepository repository = new UserRepository();
		repository.save(admin, admin.getUsername());
		repository.save(user, user.getUsername());
		return repository;
	}
	
	@Bean
	public DemoUserDetailsService demoUserDetailsService() {
		return new DemoUserDetailsService();
	}
	
	@Bean
	public StupidPasswordEncoder stupidPasswordEncoder() {
		return new StupidPasswordEncoder();
	}
	
	private RequestMatcher requireCsrfProtectionMatcher() {
	    
	    List<RequestMatcher> requestMatchers = new ArrayList<>();
	    requestMatchers.add(
	        new RegexRequestMatcher("/oauth2/authenticate.*", HttpMethod.POST));
	    requestMatchers.add(new RegexRequestMatcher(LOGIN_FORM_URL + ".*", HttpMethod.POST));
	    
	    return new OrRequestMatcher(requestMatchers);
	  }
	
	private CharacterEncodingFilter characterEncodingFilter() {

		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		return encodingFilter;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.addFilterAfter(characterEncodingFilter(), CsrfFilter.class)
			.csrf()
				.requireCsrfProtectionMatcher(requireCsrfProtectionMatcher())
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			.and().exceptionHandling()
				.authenticationEntryPoint(new DemoLoginUrlAuthenticationEntryPoint(LOGIN_FORM_URL))
			.and().formLogin().loginPage(LOGIN_FORM_URL).permitAll()
			.and().logout()
				.logoutUrl("/oauth2/logout")
			.and().authorizeRequests()
				.regexMatchers(
						LOGIN_FORM_URL + ".*",
						"/api/.*",
						"/index.html$",
						"/static/.*"
					).permitAll()
				.regexMatchers(
						// Prevent user from knowing access token and refresh token.
						"/oauth2/token$",
						"/oauth2/register.*",
						"/oauth2resx/resx"
						).anonymous()
				.anyRequest().authenticated();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(demoUserDetailsService).passwordEncoder(stupidPasswordEncoder);
	}	
}
