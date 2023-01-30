/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gevamu.corda.web.server.config;

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
