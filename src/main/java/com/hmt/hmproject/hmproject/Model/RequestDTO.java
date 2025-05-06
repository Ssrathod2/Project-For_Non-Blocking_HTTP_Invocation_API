package com.hmt.hmproject.hmproject.Model;



import com.hmt.hmproject.hmproject.Enums.ApiMethod;
import lombok.*;
import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;

@Getter                // Automatically generates getters for all fields
@Setter                // Automatically generates setters for all fields
@NoArgsConstructor     // Automatically generates a no-argument constructor
@AllArgsConstructor    // Automatically generates a constructor with all fields
@ToString              // Automatically generates a toString() method
public class RequestDTO {
    private ApiMethod apiMethod;
    private String url;
    private Map<String, String> queryParams;
    private Map<String, String> headerVariables;
    private String bodyType;
    private String requestBody;
    private Map<String, String> pathMap;
    private Map<String, String> formParam;
    private Map<String, String> urlEncodedParam;
    private List<NameValuePair> params;
}

