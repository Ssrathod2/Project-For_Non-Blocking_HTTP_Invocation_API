package com.hmt.hmproject.hmproject.Model;

import com.hmt.hmproject.hmproject.Enums.ApiMethod;

public class InvokeRequestDTO {

    private ApiMethod apiMethod;
    private RequestDTO requestDTO;
    private int timeout;

    // Getters and setters
    public ApiMethod getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(ApiMethod apiMethod) {
        this.apiMethod = apiMethod;
    }

    public RequestDTO getRequestDTO() {
        return requestDTO;
    }

    public void setRequestDTO(RequestDTO requestDTO) {
        this.requestDTO = requestDTO;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}