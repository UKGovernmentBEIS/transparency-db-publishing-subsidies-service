package com.beis.subsidy.control.accessmanagementservice.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserInGroupRequest {

    private String groupId;

    @JsonCreator
    public CreateUserInGroupRequest(
            @JsonProperty("@odata.id") String groupId) {

        this.groupId = groupId;
    }
}
