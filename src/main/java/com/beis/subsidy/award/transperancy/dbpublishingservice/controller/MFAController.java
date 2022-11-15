package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAAwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAGroupingRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAAwardRequest;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAGroupingRequest;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MfaAwardSearchInput;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MfaGroupingSearchInput;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.MFAService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.PermissionUtils;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "/mfa"
)

@Slf4j
@RestController
public class MFAController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MFAGroupingRepository mfaGroupingRepository;

    @Autowired
    private MFAAwardRepository mfaAwardRepository;

    @Autowired
    AuditLogsRepository auditLogsRepository;

    @Autowired
    private MFAService mfaService;
    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<>("Successful health check - MFA API", HttpStatus.OK);
    }

    @PostMapping(
            value = "/award/add"
    )
    public Long addMfaAward(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                 @Valid @RequestBody MFAAwardRequest mfaAwardRequest) throws JsonProcessingException {
        log.info("{} :: inside addSchemeDetails method",loggingComponentName);

        String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
        UserPrinciple userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);

        if(PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.GA_ENCODER_ROLE)){
            mfaAwardRequest.setStatus("Awaiting Approval");
        }else{
            mfaAwardRequest.setStatus("Published");
        }

        Long mfaAwardNumber = mfaService.addMfaAward(mfaAwardRequest, userPrincipleObj);

        if (mfaAwardNumber != null){
            StringBuilder eventMsg = new StringBuilder("MFA award ").append(mfaAwardNumber).append(" has been created.");

            ExcelHelper.saveAuditLogForUpdate(userPrincipleObj, "Update MFA Award", String.valueOf(mfaAwardNumber)
                    ,eventMsg.toString(),auditLogsRepository);
        }
        return mfaAwardNumber;
    }

    @GetMapping(
            value = "/award/{mfaAwardNumber}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MFAAwardResponse> findMfaAward(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                         @PathVariable("mfaAwardNumber") String mfaAwardNumber) {
        UserPrinciple userPrincipleObj = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find MFA award");
        if (StringUtils.isEmpty(mfaAwardNumber)) {
            throw new InvalidRequestException("Bad Request MFA Award is null");
        }

        if(!ExcelHelper.isNumeric(mfaAwardNumber)){
            return new ResponseEntity<MFAAwardResponse>(new MFAAwardResponse(), HttpStatus.NOT_FOUND);
        }

        Long awardNumber = Long.parseLong(mfaAwardNumber);

        MFAAwardResponse mfaAwardById = mfaService.findMfaAwardById(awardNumber);
        MFAAward mfaAward = mfaAwardRepository.findByMfaAwardNumber(mfaAwardById.getMfaAwardNumber());

        if (PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE) ||
                (PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.GA_ADMIN_ROLE) && PermissionUtils.userPrincipleContainsId(userPrinciple, mfaAward.getGrantingAuthority().getAzureGroupId()))
        ){
            mfaAwardById.setCanApprove(true);
        }

        return new ResponseEntity<MFAAwardResponse>(mfaAwardById, HttpStatus.OK);
    }

    @PutMapping(
            value="/award/update/{mfaAwardNumber}"
    )
    public String updateMfaAwardDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                      @RequestBody MFAAwardRequest mfaAwardRequest,
                                      @PathVariable("mfaAwardNumber") String mfaAwardNumber,
                                      HttpServletResponse response) {

        log.info("{} ::Before calling updateMfaGrouping", loggingComponentName);
        if(Objects.isNull(mfaAwardRequest)|| StringUtils.isEmpty(mfaAwardNumber)) {
            throw new InvalidRequestException("schemeReq is empty or scNumber");
        }

        if(!ExcelHelper.isNumeric(mfaAwardNumber)){
            throw new InvalidRequestException("award number should be numeric");
        }

        Long awardNumber = Long.parseLong(mfaAwardNumber);

        UserPrinciple userPrincipleObj = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"update MFA award");
        MFAAward mfaAward = mfaAwardRepository.findByMfaAwardNumber(awardNumber);
        // if user not BEIS Admin then ensure that they belong to the GA that owns the MFA Grouping;
        if (!PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE)) {
            if(!PermissionUtils.userPrincipleContainsId(userPrinciple, mfaAward.getGrantingAuthority().getAzureGroupId())){
                response.setStatus(403);
                log.error("User " + userPrincipleObj.getUserName() + " does not have the rights to update MFA award: " + awardNumber);
                return null;
            }
        }

        StringBuilder eventMsg = new StringBuilder("");

        // if the mfa award status has changed.
        if(!mfaAwardRequest.getStatus().equalsIgnoreCase(mfaAward.getStatus())){
            eventMsg = new StringBuilder("MFA award ").append(awardNumber).append(" is updated to ")
                    .append(mfaAwardRequest.getStatus());
            if(mfaAwardRequest.getStatus().equalsIgnoreCase("deleted")) {
                if (!PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE)
                        && !PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.GA_ADMIN_ROLE)) {
                    response.setStatus(403);
                    log.error("User " + userPrincipleObj.getUserName() + " does not have the rights to delete MFA grouping: " + awardNumber);
                    return null;
                }
            }
        }else{
            eventMsg = new StringBuilder("MFA award ").append(awardNumber).append(" has been updated.");
        }

        Long mfaAwardUpdateResponse = mfaService.updateMfaAward(mfaAwardRequest, awardNumber, userPrincipleObj);

        ExcelHelper.saveAuditLogForUpdate(userPrincipleObj, "Update MFA Award", mfaAwardUpdateResponse.toString()
                ,eventMsg.toString(),auditLogsRepository);
        log.info("{} ::end of calling updateMfaGrouping", loggingComponentName);

        return mfaAwardUpdateResponse.toString();
    }

    @PostMapping(
            value = "/award/search",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MFAAwardsResponse> findMfaAwards(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                 @Valid @RequestBody MfaAwardSearchInput searchInput){
        UserPrinciple userPrincipleResp = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find Subsidy Schema");
        if(searchInput.getTotalRecordsPerPage() == null){
            searchInput.setTotalRecordsPerPage(10);
        }
        if(searchInput.getPageNumber() == null) {
            searchInput.setPageNumber(1);
        }
        MFAAwardsResponse searchResults = mfaService.findMatchingMfaAwardDetails(searchInput,userPrincipleResp);
        return new ResponseEntity<MFAAwardsResponse>(searchResults, HttpStatus.OK);
    }

    @PostMapping(
            value = "/grouping/add"
    )
    public String addMfaGrouping(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                   @Valid @RequestBody MFAGroupingRequest mfaGroupingRequest) throws JsonProcessingException {
        log.info("{} :: inside addSchemeDetails method",loggingComponentName);

        String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
        UserPrinciple userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
        String mfaGroupingNumber = mfaService.addMfaGrouping(mfaGroupingRequest, userPrincipleObj);

        if (mfaGroupingNumber != null && mfaGroupingNumber != ""){
            StringBuilder eventMsg = new StringBuilder("MFA grouping ").append(mfaGroupingNumber).append(" has been created.");

            ExcelHelper.saveAuditLogForUpdate(userPrincipleObj, "Update MFA Grouping", mfaGroupingNumber
                    ,eventMsg.toString(),auditLogsRepository);
        }
        return mfaGroupingNumber;
    }

    @PostMapping(
            value = "/grouping/search",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MFAGroupingsResponse> findMfaGroupings(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                @Valid @RequestBody MfaGroupingSearchInput searchInput){
        UserPrinciple userPrinicipleResp = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find Subsidy Schema");
        if(searchInput.getTotalRecordsPerPage() == null){
            searchInput.setTotalRecordsPerPage(10);
        }
        if(searchInput.getPageNumber() == null) {
            searchInput.setPageNumber(1);
        }
        MFAGroupingsResponse searchResults = mfaService.findMatchingMfaGroupingDetails(searchInput,userPrinicipleResp);
        return new ResponseEntity<MFAGroupingsResponse>(searchResults, HttpStatus.OK);
    }

    @GetMapping(
            value = "/grouping/{mfaGroupingNumber}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MFAGroupingResponse> findMfaGrouping(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                 @PathVariable("mfaGroupingNumber") String mfaGroupingNumber) {
        UserPrinciple userPrincipleObj = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find Subsidy Schema");
        if (StringUtils.isEmpty(mfaGroupingNumber)) {
            throw new InvalidRequestException("Bad Request SC Number is null");
        }
        MFAGroupingResponse mfaGroupingById = mfaService.findMfaGroupingById(mfaGroupingNumber);
        MFAGrouping mfaGrouping = mfaGroupingRepository.findByMfaGroupingNumber(mfaGroupingById.getMfaGroupingNumber());

        if (PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE) ||
                (PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.GA_ADMIN_ROLE) && PermissionUtils.userPrincipleContainsId(userPrinciple, mfaGrouping.getGrantingAuthority().getAzureGroupId()))
            ){
            mfaGroupingById.setCanDelete(true);
            mfaGroupingById.setCanEdit(true);
        }

        return new ResponseEntity<MFAGroupingResponse>(mfaGroupingById, HttpStatus.OK);
    }

    @PutMapping(
            value="/grouping/update/{mfaGroupingNumber}"
    )
    public String updateMfaGroupingDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                      @RequestBody MFAGroupingRequest mfaGroupingRequest,
                                      @PathVariable("mfaGroupingNumber") String mfaGroupingNumber,
                                      HttpServletResponse response) {

        log.info("{} ::Before calling updateMfaGrouping", loggingComponentName);
        if(Objects.isNull(mfaGroupingRequest)|| StringUtils.isEmpty(mfaGroupingNumber)) {
            throw new InvalidRequestException("schemeReq is empty or scNumber");
        }

        UserPrinciple userPrincipleObj = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"update MFA grouping");
        MFAGrouping mfaGrouping = mfaGroupingRepository.findById(mfaGroupingNumber).get();
        // if user not BEIS Admin then ensure that they belong to the GA that owns the MFA Grouping;
        if (!PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE)) {
            if(!PermissionUtils.userPrincipleContainsId(userPrinciple, mfaGrouping.getGrantingAuthority().getAzureGroupId())){
                response.setStatus(403);
                log.error("User " + userPrincipleObj.getUserName() + " does not have the rights to update MFA grouping: " + mfaGroupingNumber);
                return null;
            }
        }

        StringBuilder eventMsg = new StringBuilder("");

        // if the current saved grouping is active, and being updated to deleted, check that the user has the valid role to carry out the update.
        if(mfaGroupingRequest.getStatus().equalsIgnoreCase("deleted") && mfaGrouping.getStatus().equalsIgnoreCase("active")){
            eventMsg = new StringBuilder("MFA grouping ").append(mfaGroupingNumber).append(" is updated to ")
                    .append(mfaGroupingRequest.getStatus());
            if(!PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE)
                    && !PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.GA_ADMIN_ROLE)){
                response.setStatus(403);
                log.error("User " + userPrincipleObj.getUserName() + " does not have the rights to delete MFA grouping: " + mfaGroupingNumber);
                return null;
            }
        }else{
            eventMsg = new StringBuilder("MFA grouping ").append(mfaGroupingNumber).append(" has been updated.");
        }

        String mfaGroupingUpdateResponse = mfaService.updateMfaGrouping(mfaGroupingRequest, mfaGroupingNumber, userPrincipleObj);

        ExcelHelper.saveAuditLogForUpdate(userPrincipleObj, "Update MFA Grouping", mfaGroupingUpdateResponse
                ,eventMsg.toString(),auditLogsRepository);
        log.info("{} ::end of calling updateMfaGrouping", loggingComponentName);

        return mfaGroupingNumber;
    }
}
