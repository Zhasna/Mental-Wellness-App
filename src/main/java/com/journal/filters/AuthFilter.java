package com.journal.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String path = req.getRequestURI();
		boolean isApi = path != null && path.startsWith(req.getContextPath() + "/api/");
		boolean isAuthFree = path.endsWith("/api/login") || path.endsWith("/api/register");

		if (isApi && !isAuthFree) {
			HttpSession session = req.getSession(false);
			if (session == null || session.getAttribute("userId") == null) {
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setCharacterEncoding("UTF-8");
				res.setContentType("application/json;charset=UTF-8");
				res.getWriter().write("{\"message\":\"Authentication required\"}");
				return;
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() { }
}


