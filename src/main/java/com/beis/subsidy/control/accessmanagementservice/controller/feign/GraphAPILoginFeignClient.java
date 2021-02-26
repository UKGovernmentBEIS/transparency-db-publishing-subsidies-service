package com.beis.subsidy.control.accessmanagementservice.controller.feign;

import com.beis.subsidy.control.accessmanagementservice.response.AccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "GraphAPILoginFeignClient", url = "${graphApiLoginUrl}")
public interface GraphAPILoginFeignClient {


    @PostMapping(value = "/{tenantID}/oauth2/v2.0/token",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AccessTokenResponse getAccessIdToken(@PathVariable("tenantID") String tenantID,
                                                @RequestBody MultiValueMap<String, Object> request);
}
