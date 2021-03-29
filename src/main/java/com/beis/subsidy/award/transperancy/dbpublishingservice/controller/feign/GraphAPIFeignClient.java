package  com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "GraphAPIFeignClient", url = "${graphApiUrl}")
public interface GraphAPIFeignClient {


    @GetMapping(value = "/v1.0/groups/{groupId}/members")
    @RequestLine("GET /v1.0/groups/{groupId}/members")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getUsersByGroupId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @PathVariable("groupId")  String groupId);

    @GetMapping(value = "/v1.0/users/{userId}/appRoleAssignments")
    @RequestLine("GET /v1.0/users/{userId}/appRoleAssignments")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getUserGroupName(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @PathVariable("userId")  String userId);

}
