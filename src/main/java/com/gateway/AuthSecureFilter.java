package com.gateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class AuthSecureFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(AuthSecureFilter.class);

    @Value("#{'${routes.secured}'.split(',')}")
    private List<String> securedRoutes;

    @Value("#{'${routes.unsecured}'.split(',')}")
    private List<String> unsecuredRoutes;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Value("${auth.autenticate}")
    String autenticateApi;

    private final String ERROR = "ERROR: It's forbidden to get this resource";

    @Autowired
    RestTemplate restTemplate;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        // skip if configured as unsecured route
        if (unsecuredRoutes.stream().anyMatch(
                route -> RequestContext.getCurrentContext().getRequest().getRequestURI()
                        .startsWith(route.trim(), contextPath.length()))
        ) {
            return null;
        }

        // 401 of any configured secured routes
        if (securedRoutes.stream().anyMatch(
                route -> RequestContext.getCurrentContext().getRequest().getRequestURI()
                        .startsWith(route.trim(), contextPath.length()))
        ) {
            setFailedRequest(ERROR, HttpStatus.UNAUTHORIZED.value());
            return null;
        }

        //Authorize the request
        ResponseEntity<AuthenticateResponse> response = restTemplate
                .exchange(autenticateApi, HttpMethod.POST, null, AuthenticateResponse.class);

        //Add the fresh JWT token
        RequestContext.getCurrentContext().getResponse()
                .setHeader("Authorization", response.getHeaders().getFirst("Authorization"));

        if (response.getStatusCode().isError()) {
            setFailedRequest(ERROR, response.getStatusCode().value());
            return null;
        }

        return null;
    }

    private Boolean matchSecured(String path, String url) {
        return url.contains(path);
    }

    /**
     * Reports an error message given a response body and code.
     *
     * @param body
     * @param code
     */
    private void setFailedRequest(String body, int code) {
        logger.debug("Reporting error ({}): {}", code, body);
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(code);
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(body);
            ctx.setSendZuulResponse(false);
        }
    }

}