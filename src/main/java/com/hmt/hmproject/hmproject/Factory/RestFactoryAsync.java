package com.hmt.hmproject.hmproject.Factory;

//import org.springframework.web.reactive.function.client.WebClient;

import com.hmt.hmproject.hmproject.Enums.ApiMethod;
import com.hmt.hmproject.hmproject.Model.RequestDTO;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import javax.net.ssl.SSLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RestFactoryAsync implements ApiFactoryAsync {
    private static final Logger logger = LoggerFactory.getLogger(RestFactoryAsync.class);


    @Override
    public Mono<ResponseEntity<String>> executeTarget(
            ApiMethod apiMethod,
            RequestDTO requestDTO,
            SSLConnectionSocketFactory sslFactory,
            int timeout
    ) throws SSLException {
        return switch (apiMethod) {
            case GET     -> invokeGet(    requestDTO.getUrl(), requestDTO.getHeaderVariables(), sslFactory, timeout);
            case POST    -> invokePost(   requestDTO.getUrl(), requestDTO.getHeaderVariables(), requestDTO.getParams(),
                    requestDTO.getBodyType(), requestDTO.getRequestBody(), sslFactory, timeout);
            case PUT     -> invokePut(    requestDTO.getUrl(), requestDTO.getHeaderVariables(), requestDTO.getParams(),
                    requestDTO.getBodyType(), requestDTO.getRequestBody(), sslFactory, timeout);
            case DELETE  -> invokeDelete( requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getRequestBody(), sslFactory, timeout);
            case PATCH   -> invokePatch(  requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getRequestBody(), sslFactory, timeout);
            case OPTIONS -> invokeOptions(requestDTO.getUrl(), requestDTO.getHeaderVariables(), sslFactory, timeout);
        };
    }

    private WebClient buildClient(SSLConnectionSocketFactory sslFactory, int timeoutMillis) {
        HttpClient reactorClient;
        TcpClient tcp = TcpClient.create();
                //.responseTimeout(Duration.ofMillis(timeoutMillis));
        if (sslFactory != null) {
            // Trusting every certificate This need to updated for production base.
            reactorClient = HttpClient.from(tcp.secure(spec -> {
                try {
                    spec.sslContext(SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build());
                } catch (SSLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } else {
            reactorClient = HttpClient.from(tcp);
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(reactorClient))
                .build();
    }

    private Mono<ResponseEntity<String>> exchangeToEntity(WebClient.RequestHeadersSpec<?> spec,
                                                          int timeoutMillis,
                                                          String verb,
                                                          String url) {
        return spec.exchangeToMono((ClientResponse resp) ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> ResponseEntity
                                        .status(resp.statusCode())
                                        .headers(resp.headers().asHttpHeaders())
                                        .body(body)
                                )
                )
                .timeout(Duration.ofMillis(timeoutMillis))
                .doOnError(Throwable.class, ex -> logger.error("{} call to {} failed", verb, url, ex));
    }

    public Mono<ResponseEntity<String>> invokeGet(String url,
                                                  Map<String, String> headers,
                                                  SSLConnectionSocketFactory sslFactory,
                                                  int timeoutMillis) throws SSLException {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        WebClient.RequestHeadersSpec<?> spec = client.get()
                .uri(url)
                .headers(h -> { if (headers != null) h.setAll(headers); });
        return exchangeToEntity(spec, timeoutMillis, "GET", url);
    }

    public Mono<ResponseEntity<String>> invokePost(String url,
                                                   Map<String, String> headers,
                                                   List<NameValuePair> params,
                                                   String bodyType,
                                                   String body,
                                                   SSLConnectionSocketFactory sslFactory,
                                                   int timeoutMillis) throws SSLException {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        String fullUrl = appendParams(url, params);
        WebClient.RequestBodySpec req = client.post()
                .uri(fullUrl)
                .headers(h -> { if (headers != null) h.setAll(headers); });
        if (body != null && !body.isEmpty()) {
            MediaType media = parseMediaType(bodyType);
            req = (WebClient.RequestBodySpec) req.contentType(media).bodyValue(body);
        }
        return exchangeToEntity(req, timeoutMillis, "POST", fullUrl);
    }

    public Mono<ResponseEntity<String>> invokePut(String url,
                                                  Map<String, String> headers,
                                                  List<NameValuePair> params,
                                                  String bodyType,
                                                  String body,
                                                  SSLConnectionSocketFactory sslFactory,
                                                  int timeoutMillis) throws SSLException {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        String fullUrl = appendParams(url, params);
        WebClient.RequestBodySpec req = client.put()
                .uri(fullUrl)
                .headers(h -> { if (headers != null) h.setAll(headers); });
        if (body != null && !body.isEmpty()) {
            MediaType media = parseMediaType(bodyType);
            req = (WebClient.RequestBodySpec) req.contentType(media).bodyValue(body);
        }
        return exchangeToEntity(req, timeoutMillis, "PUT", fullUrl);
    }

    public Mono<ResponseEntity<String>> invokeDelete(String url,
                                                     Map<String, String> headers,
                                                     String body,
                                                     SSLConnectionSocketFactory sslFactory,
                                                     int timeoutMillis) throws SSLException {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        WebClient.RequestHeadersSpec<?> spec;
        if (body != null && !body.isEmpty()) {
            spec = client.method(HttpMethod.DELETE)
                    .uri(url)
                    .headers(h -> { if (headers != null) h.setAll(headers); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        } else {
            spec = client.delete()
                    .uri(url)
                    .headers(h -> { if (headers != null) h.setAll(headers); });
        }
        return exchangeToEntity(spec, timeoutMillis, "DELETE", url);
    }

    public Mono<ResponseEntity<String>> invokePatch(String url,
                                                    Map<String, String> headers,
                                                    String body,
                                                    SSLConnectionSocketFactory sslFactory,
                                                    int timeoutMillis) throws SSLException {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        WebClient.RequestBodySpec req = client.patch()
                .uri(url)
                .headers(h -> { if (headers != null) h.setAll(headers); });
        if (body != null && !body.isEmpty()) {
            req = (WebClient.RequestBodySpec) req.contentType(MediaType.APPLICATION_JSON).bodyValue(body);
        }
        return exchangeToEntity(req, timeoutMillis, "PATCH", url);
    }

    public Mono<ResponseEntity<String>> invokeOptions(String url,
                                                      Map<String, String> headers,
                                                      SSLConnectionSocketFactory sslFactory,
                                                      int timeoutMillis) {
        WebClient client = buildClient(sslFactory, timeoutMillis);
        WebClient.RequestHeadersSpec<?> spec = client.options()
                .uri(url)
                .headers(h -> { if (headers != null) h.setAll(headers); });
        return exchangeToEntity(spec, timeoutMillis, "OPTIONS", url);
    }

    // Utility to URL-encode and append query params
    private String appendParams(String url, List<NameValuePair> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        String query = params.stream()
                .map(p -> URLEncoder.encode(p.getName(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return url + (url.contains("?") ? "&" : "?") + query;
    }

    // Safely parse media type, defaulting to application/json
    private MediaType parseMediaType(String bodyType) {
        try {
            return MediaType.parseMediaType(bodyType);
        } catch (IllegalArgumentException ex) {
            if (bodyType != null && !bodyType.contains("/")) {
                return MediaType.parseMediaType("application/" + bodyType);
            }
            return MediaType.APPLICATION_JSON;
        }
    }

}
