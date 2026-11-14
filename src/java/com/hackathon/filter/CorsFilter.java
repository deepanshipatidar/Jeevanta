package com.hackathon.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Global CORS filter that intercepts <strong>every</strong> incoming request
 * and appends the headers required for cross-origin access.
 *
 * <p>This is necessary when the React frontend (e.g. {@code http://localhost:3000})
 * calls the Servlet backend (e.g. {@code http://localhost:8080}). Without these
 * headers the browser blocks the response before JavaScript can read it.
 *
 * <h3>What this filter does</h3>
 * <ol>
 *   <li>Sets {@code Access-Control-Allow-Origin: *} on every response.</li>
 *   <li>Advertises the allowed HTTP methods and headers.</li>
 *   <li>Short-circuits {@code OPTIONS} preflight requests with a {@code 200 OK}
 *       so the browser doesn't wait for a full servlet round-trip.</li>
 * </ol>
 *
 * <p>The {@code @WebFilter("/*")} annotation registers this filter for all URL
 * patterns automatically — no {@code web.xml} entry needed.
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialisation required
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // ── Append CORS headers to every response ──────────────────
        httpResp.setHeader("Access-Control-Allow-Origin",  "http://localhost:3000");
        httpResp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        httpResp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        httpResp.setHeader("Access-Control-Max-Age",       "3600");

        // ── Short-circuit OPTIONS preflight requests ───────────────
        if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
            httpResp.setStatus(HttpServletResponse.SC_OK);
            return; // Do NOT forward to the servlet — preflight is done
        }

        // ── Continue the filter chain for all other methods ────────
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup required
    }
}
