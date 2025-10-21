package com.journal.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse http = (HttpServletResponse) response;
		http.setHeader("X-Content-Type-Options", "nosniff");
		http.setHeader("X-Frame-Options", "DENY");
		http.setHeader("Referrer-Policy", "no-referrer");
		http.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
		// CSP allowing Google Fonts, Chart.js CDN, inline handlers, and local/media sources
		http.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; img-src 'self' data:; media-src 'self' blob: data:; object-src 'none'; frame-ancestors 'none'; base-uri 'self'");
		// Reduce client caching to ensure fresh assets after deploy
		http.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
		http.setHeader("Pragma", "no-cache");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() { }
}


