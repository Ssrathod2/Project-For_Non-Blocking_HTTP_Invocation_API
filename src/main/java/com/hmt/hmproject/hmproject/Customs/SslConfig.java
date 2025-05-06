package com.hmt.hmproject.hmproject.Customs;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class SslConfig {

    @Bean
    public SSLConnectionSocketFactory sslConnectionSocketFactory() throws Exception {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true; // Trust all certs
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    }
}

