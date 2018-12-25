package com.zxuanyu.gateway.filter;


import com.zxuanyu.gateway.filter.TokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Component
public class SchemeFilter implements GlobalFilter, Ordered {

    @Autowired
    TokenFilter tokenFilter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        HashSet<String> method = new HashSet<>();
        method.add("GET");
        method.add("POST");
        method.add("OPTIONS");

        if (!method.contains(exchange.getRequest().getMethod().name().toUpperCase())) {
            Map<String, Object> item = response(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
            String str = com.alibaba.fastjson.JSON.toJSONString(item);

            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            return exchange.getResponse().writeWith(Flux.just(buffer));
        }

        Object uriObj = exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);


        if (uriObj != null) {
            URI uri = (URI) uriObj;
            uri = this.upgradeConnection(uri, "http");
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, uri);
            String ret = checkToken(exchange, uri);
            if (!"200".equals(ret)) {
                byte[] bytes = getUnAuthorized();
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Flux.just(buffer));
            }
        }
        return chain.filter(exchange);
    }

    final String OK_STATUS = "200";

    private String checkToken(ServerWebExchange exchange, URI uri) {
        Object uri2 = exchange.getAttributes().get(GATEWAY_ROUTE_ATTR);
        boolean isAnon = false;

        if (uri == null) {
            return OK_STATUS;
        }

        //查找路由
        Route rt = (Route) uri2;
        String r = rt.getUri().getAuthority().toLowerCase();
        if (StringUtils.hasText(r)) {
            String path = "/" + r + uri.getPath();
            for (String item : tokenFilter.getAnonMap()) {
                if (PatternMatchUtils.simpleMatch(item, path)) {
                    isAnon = true;
                    break;
                }
            }

            if (isAnon) {
                return OK_STATUS;
            }

            List<String> authorization = exchange.getRequest().getHeaders().get("token");
            if (CollectionUtils.isEmpty(authorization)) {
                return "401";
            }
//            验证token
//            return ret;
        }

        return "200";
    }

    private byte[] getUnAuthorized() {
        Map<String, Object> item = response(401, "UNAUTHORIZED");
        String str = com.alibaba.fastjson.JSON.toJSONString(item);

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return bytes;
    }

    private URI upgradeConnection(URI uri, String scheme) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri).scheme(scheme);
        if (uri.getRawQuery() != null) {
            // When building the URI, UriComponentsBuilder verify the allowed characters and does not
            // support the '+' so we replace it for its equivalent '%20'.
            // See issue https://jira.spring.io/browse/SPR-10172
            uriComponentsBuilder.replaceQuery(uri.getRawQuery().replace("+", "%20"));
        }
        return uriComponentsBuilder.build(true).toUri();
    }

    @Override
    public int getOrder() {
        return 10101;
    }

    public static Map<String, Object> response(int status, String errorMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", status);
        map.put("ret", status);
        map.put("message", errorMessage);
        map.put("data", null);
        return map;
    }
}

