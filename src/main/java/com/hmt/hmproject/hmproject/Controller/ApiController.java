package com.hmt.hmproject.hmproject.Controller;

import com.hmt.hmproject.hmproject.Factory.RestFactory;
import com.hmt.hmproject.hmproject.Model.InvokeRequestDTO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/invoke")
public class ApiController {
    private final RestFactory restFactory;
    private final SSLConnectionSocketFactory sslConnectionSocketFactory;

    public ApiController(RestFactory restFactory , SSLConnectionSocketFactory sslConnectionSocketFactory) {
        this.restFactory = restFactory;
        this.sslConnectionSocketFactory = sslConnectionSocketFactory;
        System.out.println(this.sslConnectionSocketFactory);
    }

    @PostMapping
    public ResponseEntity<String> post(@RequestBody InvokeRequestDTO request) {
        return invoke(request);
    }

    @GetMapping
    public ResponseEntity<String> get(@RequestBody InvokeRequestDTO request) {
        return invoke(request);
    }

    @PutMapping
    public ResponseEntity<String> put(@RequestBody InvokeRequestDTO request) {
        return invoke(request);
    }

    @DeleteMapping
    public ResponseEntity<String> delete(@RequestBody InvokeRequestDTO request) {
        return invoke(request);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<String> options(@RequestBody InvokeRequestDTO request) {
        return invoke(request);
    }

    private ResponseEntity<String> invoke(InvokeRequestDTO request) {
        try {
            CloseableHttpResponse response = restFactory.executeTarget(
                    request.getApiMethod(),
                    request.getRequestDTO(),
                    sslConnectionSocketFactory,
                    request.getTimeout()
            );

            String responseBody = EntityUtils.toString(response.getEntity());
            return ResponseEntity.ok(responseBody);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Request failed: " + e.getMessage());
        }
    }
/*
    @PostMapping
    public ResponseEntity<String> invoke(@RequestBody InvokeRequestDTO request) {
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
