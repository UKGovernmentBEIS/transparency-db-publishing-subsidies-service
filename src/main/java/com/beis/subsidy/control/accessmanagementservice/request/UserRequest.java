package com.beis.subsidy.control.accessmanagementservice.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
public class UserRequest {

    private boolean accountEnabled;
    private String displayName;
    private String mailNickname;
    private String userPrincipalName;

    private String mobilePhone;

    private Set<String> grpRoleIds;

    @NotNull
    private PasswordProfile passwordProfile;

    @JsonCreator
    public UserRequest(
            @JsonProperty("accountEnabled") boolean accountEnabled,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("mailNickname") String mailNickname,
            @JsonProperty("userPrincipalName") String userPrincipalName,
            @JsonProperty("mobilePhone") String mobilePhone,
            @JsonProperty("passwordProfile") PasswordProfile passwordProfile,
            @JsonProperty("grpRoleIds") Set<String> grpRoleIds) {

        this.accountEnabled = accountEnabled;
        this.displayName = displayName;
        this.mailNickname = mailNickname;
        this.userPrincipalName = userPrincipalName;
        this.mobilePhone = mobilePhone;
        this.passwordProfile = passwordProfile;
        this.grpRoleIds = grpRoleIds;
    }
}
