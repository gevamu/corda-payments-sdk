package com.gevamu.web.server.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IndexFallbackWebFilter implements WebFilter {

    private static final String ROOT = "/";
    private static final String INDEX = "/index.html";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return isRequestToRoot(exchange) ?
            redirectToIndex(exchange, chain) :
            chain.filter(exchange);
    }

    private boolean isRequestToRoot(ServerWebExchange exchange) {
        return ROOT.equals(exchange.getRequest().getURI().getPath());
    }

    private Mono<Void> redirectToIndex(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest redirectRequest = exchange.getRequest()
            .mutate()
            .path(INDEX)
            .build();
        ServerWebExchange redirectExchange = exchange.mutate()
            .request(redirectRequest)
            .build();
        return chain.filter(redirectExchange);
    }
}
