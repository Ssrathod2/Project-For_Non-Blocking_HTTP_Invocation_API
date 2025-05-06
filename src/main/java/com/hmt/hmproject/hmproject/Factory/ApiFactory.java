package com.hmt.hmproject.hmproject.Factory;


import com.hmt.hmproject.hmproject.Enums.ApiMethod;
import com.hmt.hmproject.hmproject.Model.RequestDTO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import java.io.IOException;

public interface ApiFactory {
    public abstract CloseableHttpResponse executeTarget(
            ApiMethod apiMethod,
            RequestDTO requestDTO,
            SSLConnectionSocketFactory sslConnectionSocketFactory,
            int timeout
    ) throws IOException;
}

