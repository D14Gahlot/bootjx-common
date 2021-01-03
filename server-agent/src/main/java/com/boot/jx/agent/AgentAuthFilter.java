package com.boot.jx.agent;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;

@Component
public class AgentAuthFilter implements Filter {

	/** The session service. */
	@Autowired
	AgentSessionBean sessionService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		//if (!(sessionService.isLoggedIn() && ArgUtil.is(sessionService.getAgentCode()))) {
			//HttpServletResponse response = ((HttpServletResponse) resp);
			//response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			//response.setHeader("Location", "/agent/auth/login?logout");
		//} else {
			chain.doFilter(req, resp);
		//}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// empty
	}

}
