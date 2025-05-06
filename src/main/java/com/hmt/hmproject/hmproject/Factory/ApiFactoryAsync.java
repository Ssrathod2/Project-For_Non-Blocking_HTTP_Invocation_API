package com.hmt.hmproject.hmproject.Factory;


import com.hmt.hmproject.hmproject.Enums.ApiMethod;
import com.hmt.hmproject.hmproject.Model.RequestDTO;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface ApiFactoryAsync {
    public abstract Mono<ResponseEntity<String>> executeTarget(
            ApiMethod apiMethod,
            RequestDTO requestDTO,
            SSLConnectionSocketFactory sslConnectionSocketFactory,
            int timeout
    ) throws IOException;
}

