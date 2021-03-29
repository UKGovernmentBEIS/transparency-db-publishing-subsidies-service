package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    @JsonProperty
    private String id;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String givenName;

    @JsonProperty
    private String jobTitle;

    @JsonProperty
    private String mail;

    @JsonProperty
    private String mobilePhone;

    @JsonProperty
    private String surname;

    @JsonProperty
    private String userPrincipalName;

    @JsonProperty
    private String roleName;

    @JsonProperty
    private InviteUserResponse invitedUser;
  }
