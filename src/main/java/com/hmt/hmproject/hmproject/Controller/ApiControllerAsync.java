package com.hmt.hmproject.hmproject.Controller;

import com.hmt.hmproject.hmproject.Factory.RestFactoryAsync;
import com.hmt.hmproject.hmproject.Model.InvokeRequestDTO;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import java.io.IOException;

@RestController
@RequestMapping("/invokeasync")
public class ApiControllerAsync {
    @Autowired
    private final RestFactoryAsync restFactoryAsync;
    private final SSLConnectionSocketFactory sslConnectionSocketFactory;

    public ApiControllerAsync (RestFactoryAsync restFactoryAsync , SSLConnectionSocketFactory sslConnectionSocketFactory) {
        this.restFactoryAsync = restFactoryAsync;
        this.sslConnectionSocketFactory = sslConnectionSocketFactory;
        System.out.println(this.sslConnectionSocketFactory);
    }

    @PostMapping
    public Mono<ResponseEntity<String>> post(@RequestBody InvokeRequestDTO request) throws SSLException {
        return invoke(request);
    }

    @GetMapping
    public Mono<ResponseEntity<String>> get(@RequestBody InvokeRequestDTO request) throws SSLException {
        return invoke(request);
    }

    @PutMapping
    public Mono<ResponseEntity<String>> put(@RequestBody InvokeRequestDTO request) throws SSLException {
        return invoke(request);
    }

    @DeleteMapping
    public Mono<ResponseEntity<String>> delete(@RequestBody InvokeRequestDTO request) throws SSLException {
        return invoke(request);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public Mono<ResponseEntity<String>> options(@RequestBody InvokeRequestDTO request) throws SSLException {
        return invoke(request);
    }

    private Mono<ResponseEntity<String>> invoke(InvokeRequestDTO request) throws SSLException {
        return restFactoryAsync.executeTarget(
                        request.getApiMethod(),
                        request.getRequestDTO(),
                        sslConnectionSocketFactory,
                        request.getTimeout()
                )
                // map the downstream ResponseEntity<String> straight through
                .map(downstream ->
                        ResponseEntity
                                .status(downstream.getStatusCode())
                                .headers(downstream.getHeaders())
                                .body(downstream.getBody())
                )
                // IO failures (e.g. timeouts)
                .onErrorResume(e -> e instanceof IOException,
                        e -> Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Request failed: " + e.getMessage())
                        )
                )
                // HTTP 4xx/5xx from WebClient
                .onErrorResume(e -> e instanceof WebClientResponseException, e -> {
                    WebClientResponseException ex = (WebClientResponseException) e;
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body("Downstream error: " + ex.getResponseBodyAsString())
                    );
                });
    }
/*
    @PostMapping
    public Mono<ResponseEntity<String>> invoke(@RequestBody InvokeRequestDTO request) {
        try {
            CloseableHttpResponse response = restFactory.executeTarget(
                    request.getApiMethod(),
                    request.getRequestDTO(),
                    sslConnectionSocketFactory,
                    request.getTimeout());

            String responseBody = EntityUtils.toString(response.getEntity());
            return ResponseEntity.ok(responseBody);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed: " + e.getMessage());
        }
    }

 */

}

