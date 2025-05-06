package com.hmt.hmproject.hmproject.Factory;



import com.hmt.hmproject.hmproject.Customs.HttpDeleteWithBody;
import com.hmt.hmproject.hmproject.Enums.ApiMethod;
import com.hmt.hmproject.hmproject.Model.RequestDTO;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RestFactory implements ApiFactory{


    private static final Logger logger = LoggerFactory.getLogger(RestFactory.class);


    @Override
    public CloseableHttpResponse executeTarget(ApiMethod apiMethod, RequestDTO requestDTO,
                                               SSLConnectionSocketFactory sslConnectionSocketFactory, int timeout) throws IOException {
        return switch (apiMethod) {
            case GET -> invokeGet(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    sslConnectionSocketFactory, timeout);
            case POST -> invokePost(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getParams(), requestDTO.getBodyType(), requestDTO.getRequestBody(),
                    sslConnectionSocketFactory, timeout);
            case PUT -> invokePut(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getParams(), requestDTO.getBodyType(), requestDTO.getRequestBody(),
                    sslConnectionSocketFactory, timeout);
            case DELETE -> invokeDelete(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getRequestBody(), sslConnectionSocketFactory, timeout);
            case PATCH -> invokePatch(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    requestDTO.getRequestBody(), sslConnectionSocketFactory, timeout);
            case OPTIONS -> invokeOptions(requestDTO.getUrl(), requestDTO.getHeaderVariables(),
                    sslConnectionSocketFactory, timeout);
        };
    }


    public CloseableHttpResponse invokeGet(String url, Map<String, String> headers,
                                  SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws IOException {
        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpGet request = new HttpGet(url);
        addHeaders(request, headers);
//        long startTime = System.currentTimeMillis();
        return client.execute(request);
    }


    public CloseableHttpResponse invokePost(String url, Map<String, String> headers, List<NameValuePair> params,
                                   String type, String body, SSLConnectionSocketFactory sslConnectionFactory, int timeout)
            throws IOException {
        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPost request = new HttpPost(url);
        updatePostPutRequest(headers, params, type, body, request);
        return client.execute(request);
    }


    public CloseableHttpResponse invokePut(String url, Map<String, String> headers, List<NameValuePair> params,
                                  String type, String body, SSLConnectionSocketFactory sslConnectionFactory, int timeout)
            throws IOException {


        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPut request = new HttpPut(url);
        updatePostPutRequest(headers, params, type, body, request);
        return client.execute(request);
    }
    public CloseableHttpResponse invokeDelete(String url, Map<String, String> headers, String body,
                                     SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws IOException {
        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpRequestBase request;
        if(null != body && !body.isEmpty()){
            request = new HttpDeleteWithBody(url);
            addBody(request, body);
        } else {
            request=new HttpDelete(url);
        }
        addHeaders(request, headers);
        return client.execute(request);
    }
    public CloseableHttpResponse invokePatch(String url, Map<String, String> headers, String body,
                                    SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws IOException {
        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPatch request = new HttpPatch(url);
        request.setEntity(new StringEntity(body));
        addHeaders(request, headers);
        return client.execute(request);
    }
    public CloseableHttpResponse invokeOptions(String url, Map<String, String> headers,
                                      SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws IOException {
        CloseableHttpClient client = getClient(sslConnectionFactory, timeout);
        HttpOptions request = new HttpOptions(url);
        addHeaders(request, headers);
        return client.execute(request);
    }


    private void updatePostPutRequest(Map<String, String> headers, List<NameValuePair> params, String type,
                                      String body, HttpEntityEnclosingRequestBase request) throws UnsupportedEncodingException {
        if (body == null && type != null) {
            if (type.equals("multi-part")) {
                headers.remove("Content-Type");
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                if(params != null) {
                    for (NameValuePair param : params) {
                        entityBuilder.addTextBody(param.getName(), param.getValue(), ContentType.DEFAULT_BINARY);
                    }
                }
                request.setEntity(entityBuilder.build());
            } else {
                List<NameValuePair> formParams = new ArrayList<>();
                if(params != null) {
                    for (NameValuePair param : params) {
                        formParams.add(new BasicNameValuePair(param.getName(), param.getValue()));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                request.setEntity(entity);
            }


        } else if(body != null){
            request.setEntity(new StringEntity(body));
        }
        addHeaders(request, headers);
    }


    private CloseableHttpClient getClient(SSLConnectionSocketFactory sslConnectionFactory, int timeout) {
        CloseableHttpClient client;
        if (sslConnectionFactory != null) {
            logger.debug("Getting client details");
            client = HttpClientBuilder.create().setSSLSocketFactory(sslConnectionFactory)
                    .setDefaultRequestConfig(requestConfigWithTimeout(timeout)).build();
        } else {
            client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfigWithTimeout(timeout)).build();
        }
        return client;
    }


    public RequestConfig requestConfigWithTimeout(int timeoutInMilliseconds) {
        return RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(timeoutInMilliseconds)
                .setConnectTimeout(timeoutInMilliseconds).setConnectionRequestTimeout(timeoutInMilliseconds).build();
    }


    private void addBody(HttpRequestBase request, String body) {
        if(null != body && !"".equalsIgnoreCase(body)) {
            StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        }
    }


    private void addHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
    }




}
