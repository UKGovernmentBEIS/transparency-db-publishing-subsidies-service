package com.beis.subsidy.control.accessmanagementservice.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class UserRolesResponse {

    @JsonProperty
    private List<UserRoleResponse> value = new ArrayList<>();

    @JsonGetter("value")
    public List<UserRoleResponse> getUserRoles() {
        return value;
    }

    @JsonSetter("value")
    public void setUserRoles(List<UserRoleResponse> value) {
        this.value = value;
    }

  }
