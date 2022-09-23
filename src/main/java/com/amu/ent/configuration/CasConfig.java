package com.amu.ent.configuration;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import static com.amu.ent.auth.security.SecurityConstants.SIGN_UP_URL;

@Configuration
public class CasConfig {

	@Autowired
	SpringCasAutoconfig autoconfig;

	private static boolean casEnabled = true;

	public CasConfig() {
	}

	@Bean
	public SpringCasAutoconfig getSpringCasAutoconfig() {
		return new SpringCasAutoconfig();
	}

	/**
	 * For single point logout
	 */
	@Bean
	public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {
		ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listener = new ServletListenerRegistrationBean<>();
		listener.setEnabled(casEnabled);
		listener.setListener(new SingleSignOutHttpSessionListener());
		listener.setOrder(1);
		return listener;
	}

	/**
	 * The filter is used for single point logout ， Single point exit configuration
	 * ， Be sure to put something else on filter beforemessage
	 */
	@Bean
	public FilterRegistrationBean logOutFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		LogoutFilter logoutFilter = new LogoutFilter(
				autoconfig.getCasServerUrlPrefix() + "/logout?service=" + autoconfig.getServerName(),
				new SecurityContextLogoutHandler());
		filterRegistration.setFilter(logoutFilter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/logout");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(2);
		return filterRegistration;
	}

	/**
	 * The filter is used for single point logout ， Single point exit configuration
	 * ， Be sure to put something else on filter before
	 */
	@Bean
	public FilterRegistrationBean singleSignOutFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new SingleSignOutFilter());
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getSignOutFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getSignOutFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(3);
		return filterRegistration;
	}

	/**
	 * The filter is responsible for user authentication
	 */
	@Bean
	public FilterRegistrationBean authenticationFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new AuthenticationFilter());
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getAuthFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAuthFilters());
		else
			filterRegistration.addUrlPatterns(SIGN_UP_URL);
		// casServerLoginUrl:cas Service landing url
		filterRegistration.addInitParameter("casServerLoginUrl", autoconfig.getCasServerLoginUrl());
		// Login to this project ip+port
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.addInitParameter("useSession", autoconfig.isUseSession() ? "true" : "false");
		filterRegistration.addInitParameter("redirectAfterValidation",
				autoconfig.isRedirectAfterValidation() ? "true" : "false");
		filterRegistration.setOrder(4);
		return filterRegistration;
	}

	/**
	 * The filter is responsible for Ticket Calibration work
	 */
	@Bean
	public FilterRegistrationBean cas20ProxyReceivingTicketValidationFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		Cas20ProxyReceivingTicketValidationFilter cas20ProxyReceivingTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
		// cas20ProxyReceivingTicketValidationFilter.setTicketValidator(cas20ServiceTicketValidator());
		cas20ProxyReceivingTicketValidationFilter.setServerName(autoconfig.getServerName());
		filterRegistration.setFilter(cas20ProxyReceivingTicketValidationFilter);
		filterRegistration.setEnabled(casEnabled);
		if (autoconfig.getValidateFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getValidateFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.addInitParameter("redirectAfterValidation",
				autoconfig.isRedirectAfterValidation() ? "true" : "false");
		filterRegistration.addInitParameter("casServerUrlPrefix", autoconfig.getCasServerUrlPrefix());
		filterRegistration.addInitParameter("serverName", autoconfig.getServerName());
		filterRegistration.setOrder(5);
		return filterRegistration;
	}

	/**
	 * The filter pair HttpServletRequest Request packaging ， Accessible
	 * HttpServletRequest The getRemoteUser() Method to get the login name of the
	 * logged in user
	 * 
	 */
	@Bean
	public FilterRegistrationBean httpServletRequestWrapperFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new HttpServletRequestWrapperFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getRequestWrapperFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getRequestWrapperFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.setOrder(6);
		return filterRegistration;
	}

	/**
	 * The filter makes it possible to pass
	 * org.jasig.cas.client.util.AssertionHolder To get the user's login name 。 such
	 * as AssertionHolder.getAssertion().getPrincipal().getName()。 This class
	 * handles Assertion Information on ThreadLocal Variable ， The application is
	 * not in use web Layer is also able to retrieve the current logon information
	 */
	@Bean
	public FilterRegistrationBean assertionThreadLocalFilter() {
		FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new AssertionThreadLocalFilter());
		filterRegistration.setEnabled(true);
		if (autoconfig.getAssertionFilters().size() > 0)
			filterRegistration.setUrlPatterns(autoconfig.getAssertionFilters());
		else
			filterRegistration.addUrlPatterns("/*");
		filterRegistration.setOrder(7);
		return filterRegistration;
	}
}