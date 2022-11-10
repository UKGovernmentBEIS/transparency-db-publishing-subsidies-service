package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.award.transperancy.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.award.transperancy.dbpublishingservice.exception.UnauthorisedAccessException;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAAwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAGroupingRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAAwardRequest;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MFAGroupingRequest;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MfaAwardSearchInput;
import com.beis.subsidy.award.transperancy.dbpublishingservice.request.MfaGroupingSearchInput;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.MFAGroupingSpecificaionUtils;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.PermissionUtils;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.SearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class MFAService {

    @Autowired
    private MFAGroupingRepository mfaGroupingRepository;

    @Autowired
    private MFAAwardRepository mfaAwardRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private LocalDate convertToLocalDate(String incomingDate) {
        return LocalDate.parse(incomingDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String addMfaGrouping(MFAGroupingRequest mfaGroupingRequest, UserPrinciple userPrinciple) {
        MFAGrouping mfaGroupingToSave = new MFAGrouping();

        if (!StringUtils.isEmpty(mfaGroupingRequest.getMfaGroupingName())) {
            mfaGroupingToSave.setMfaGroupingName(mfaGroupingRequest.getMfaGroupingName().trim());
        }

        if (!StringUtils.isEmpty(mfaGroupingRequest.getGrantingAuthorityName())) {
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(mfaGroupingRequest.getGrantingAuthorityName().trim());

            log.error("{} :: Granting Authority and GAName ::{}", grantingAuthority, mfaGroupingRequest.getGrantingAuthorityName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

                log.error("{} :: Granting Authority is Inactive for the scheme");
                throw new InvalidRequestException("Granting Authority is Inactive");
            }
            mfaGroupingToSave.setGaId(grantingAuthority.getGaId());
        }

        if (!StringUtils.isEmpty(mfaGroupingRequest.getStatus())) {
            mfaGroupingToSave.setStatus(mfaGroupingRequest.getStatus());
        }

        mfaGroupingToSave.setCreatedBy(userPrinciple.getUserName());
        mfaGroupingToSave.setCreatedTimestamp(LocalDateTime.now());
        mfaGroupingToSave.setLastModifiedTimestamp(LocalDateTime.now());

        MFAGrouping savedMfaGrouping = mfaGroupingRepository.save(mfaGroupingToSave);

        return savedMfaGrouping.getMfaGroupingNumber();
    }

    public Long addMfaAward(MFAAwardRequest mfaAwardRequest, UserPrinciple userPrinciple) {
        MFAAward mfaAwardToSave = new MFAAward();

        mfaAwardToSave.setSPEI(mfaAwardRequest.isSpeiAssistance());
        mfaAwardToSave.setMfaGroupingPresent(mfaAwardRequest.isMfaGroupingPresent());

        if(mfaAwardRequest.isMfaGroupingPresent() && !StringUtils.isEmpty(mfaAwardRequest.getMfaGroupingId())){
            mfaAwardToSave.setMfaGroupingNumber(mfaAwardRequest.getMfaGroupingId());
            MFAGrouping mfaGrouping = mfaGroupingRepository.findByMfaGroupingNumber(mfaAwardRequest.getMfaGroupingId());
            if(mfaGrouping != null){
                mfaAwardToSave.setMfaGrouping(mfaGrouping);
            }
        }

        if(mfaAwardRequest.getAwardFullAmount() != null){
            mfaAwardToSave.setAwardAmount(mfaAwardRequest.getAwardFullAmount());
        }

        if(!StringUtils.isEmpty(mfaAwardRequest.getConfirmationDate()) && mfaAwardRequest.getConfirmationDate() != null){
            mfaAwardToSave.setConfirmationDate(convertToLocalDate(mfaAwardRequest.getConfirmationDate()));
        }

        if (!StringUtils.isEmpty(mfaAwardRequest.getGrantingAuthorityName())) {
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(mfaAwardRequest.getGrantingAuthorityName().trim());

            log.error("{} :: Granting Authority and GAName ::{}", grantingAuthority, mfaAwardRequest.getGrantingAuthorityName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

                log.error("{} :: Granting Authority is Inactive for the scheme");
                throw new InvalidRequestException("Granting Authority is Inactive");
            }
            mfaAwardToSave.setGaId(grantingAuthority.getGaId());
        }

        if(!StringUtils.isEmpty(mfaAwardRequest.getBeneficiaryName()) && mfaAwardRequest.getBeneficiaryName() != null){
            mfaAwardToSave.setRecipientName(mfaAwardRequest.getBeneficiaryName());
        }

        if(!StringUtils.isEmpty(mfaAwardRequest.getNationalIdType()) && mfaAwardRequest.getNationalIdType() != null){
            mfaAwardToSave.setRecipientIdType(mfaAwardRequest.getNationalIdType());
        }

        if(!StringUtils.isEmpty(mfaAwardRequest.getNationalIdNumber()) && mfaAwardRequest.getNationalIdNumber() != null){
            mfaAwardToSave.setRecipientId(mfaAwardRequest.getNationalIdNumber());
        }

        if (!StringUtils.isEmpty(mfaAwardRequest.getStatus())) {
            mfaAwardToSave.setStatus(mfaAwardRequest.getStatus());
        }

        if(!PermissionUtils.userHasRole(userPrinciple, AccessManagementConstant.GA_ENCODER_ROLE)){
            mfaAwardToSave.setApprovedBy(userPrinciple.getUserName());
        }

        mfaAwardToSave.setCreatedBy(userPrinciple.getUserName());
        mfaAwardToSave.setCreatedTimestamp(LocalDateTime.now());
        mfaAwardToSave.setLastModifiedTimestamp(LocalDateTime.now());

        MFAAward savedMfaAward = mfaAwardRepository.save(mfaAwardToSave);

        return savedMfaAward.getMfaAwardNumber();
    }

    public MFAGroupingsResponse findMatchingMfaGroupingDetails(MfaGroupingSearchInput searchInput, UserPrinciple userPriniciple) {
        Specification<MFAGrouping> mfaGroupingSpecifications = getSpecificationMFAGroupingDetails(searchInput, true);
        Specification<MFAGrouping> mfaGroupingSpecificationsWithout = getSpecificationMFAGroupingDetails(searchInput, false);
        List<MFAGrouping> totalMfaGroupingsList = new ArrayList<>();
        List<MFAGrouping> mfaGroupingResults = null;
        Page<MFAGrouping> pageMfaGroupings = null;
        MFAGroupingsResponse mfaGroupingsResponse = new MFAGroupingsResponse();

        List<Sort.Order> orders = SearchUtils.getOrderByCondition(searchInput.getSortBy());

        Pageable pagingSortMfaGroupings = PageRequest.of(searchInput.getPageNumber() - 1,
                searchInput.getTotalRecordsPerPage(), Sort.by(orders));

        if (AccessManagementConstant.BEIS_ADMIN_ROLE.equals(userPriniciple.getRole().trim())) {
            pageMfaGroupings = mfaGroupingRepository.findAll(mfaGroupingSpecifications, pagingSortMfaGroupings);
            mfaGroupingResults = pageMfaGroupings.getContent();

            if (!StringUtils.isEmpty(searchInput.getSearchName())) {
                totalMfaGroupingsList = mfaGroupingRepository.findAll(mfaGroupingSpecificationsWithout);
            } else {
                totalMfaGroupingsList = mfaGroupingRepository.findAll();
            }
        }else{
            if (!StringUtils.isEmpty(searchInput.getSearchName())
                    || !StringUtils.isEmpty(searchInput.getStatus())) {

                mfaGroupingSpecifications = getSpecificationMfaGroupingsForGARoles(searchInput,userPriniciple.getGrantingAuthorityGroupName());
                pageMfaGroupings = mfaGroupingRepository.findAll(mfaGroupingSpecifications, pagingSortMfaGroupings);

                mfaGroupingResults = pageMfaGroupings.getContent();
                totalMfaGroupingsList = mfaGroupingRepository.findAll(mfaGroupingSpecifications);

            } else {

                Long gaId = getGrantingAuthorityIdByName(userPriniciple.getGrantingAuthorityGroupName());
                if(gaId == null || gaId <= 0){
                    throw new UnauthorisedAccessException("Invalid granting authority name");
                }
                pageMfaGroupings = mfaGroupingRepository.
                        findAll(mfaGroupingByGrantingAuthority(gaId),pagingSortMfaGroupings);
                mfaGroupingResults = pageMfaGroupings.getContent();
                totalMfaGroupingsList = mfaGroupingRepository.findAll(mfaGroupingByGrantingAuthority(gaId));

            }
        }

        if (!mfaGroupingResults.isEmpty()) {
            mfaGroupingsResponse = new MFAGroupingsResponse(mfaGroupingResults, pageMfaGroupings.getTotalElements(),
                    pageMfaGroupings.getNumber() + 1, pageMfaGroupings.getTotalPages(), mfaGroupingCounts(totalMfaGroupingsList));
        } else {
            log.info("{}::Scheme results not found");
            throw new SearchResultNotFoundException("MFA Grouping Results NotFound");
        }
        return mfaGroupingsResponse;
    }

    public MFAAwardsResponse findMatchingMfaAwardDetails(MfaAwardSearchInput searchInput, UserPrinciple userPrinicipleResp) {
        return null;
    }

    public MFAGroupingResponse findSubsidySchemeById(String mfaGroupingNumber) {
        MFAGrouping mfaGrouping = mfaGroupingRepository.findById(mfaGroupingNumber).get();
        return new MFAGroupingResponse(mfaGrouping);
    }

    private Specification<MFAGrouping> getSpecificationMFAGroupingDetails(MfaGroupingSearchInput searchInput, boolean withStatus) {
        String searchName = searchInput.getSearchName();
        Specification<MFAGrouping> mfaGroupingSpecifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : MFAGroupingSpecificaionUtils.mfaGroupingNameLike(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : MFAGroupingSpecificaionUtils.mfaGroupingNumberEquals(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :MFAGroupingSpecificaionUtils.grantingAuthorityNameEqual(searchName.trim()))
                );

        if (withStatus) {
            mfaGroupingSpecifications = mfaGroupingSpecifications.and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                    ? null : MFAGroupingSpecificaionUtils.mfaGroupingByStatus(searchInput.getStatus().trim()));
        }
        return mfaGroupingSpecifications;
    }

    private Map<String, Long> mfaGroupingCounts(List<MFAGrouping> mfaGroupingsList) {
        long allMfaGroupings = mfaGroupingsList.size();
        long activeMfaGroupings = 0;
        long deletedMfaGroupings = 0;

        if (mfaGroupingsList != null && mfaGroupingsList.size() > 0) {
            for (MFAGrouping mfaGrouping : mfaGroupingsList) {
                if (mfaGrouping.getStatus().equalsIgnoreCase(AccessManagementConstant.ACTIVE)) {
                    activeMfaGroupings++;
                }
                if (mfaGrouping.getStatus().equalsIgnoreCase(AccessManagementConstant.DELETED)) {
                    deletedMfaGroupings++;
                }
            }
        }
        Map<String, Long> smUserActivityCount = new HashMap<>();
        smUserActivityCount.put("allMfaGroupings", allMfaGroupings);
        smUserActivityCount.put("activeMfaGroupings", activeMfaGroupings);
        smUserActivityCount.put("deletedMfaGroupings", deletedMfaGroupings);
        return smUserActivityCount;
    }

    public String updateMfaGrouping(MFAGroupingRequest mfaGroupingRequest, String mfaGroupingNumber, UserPrinciple userPrincipleObj) {
        log.info("Inside updateSubsidySchemeDetails method - sc number " + mfaGroupingRequest.getMfaGroupingNumber());
        MFAGrouping mfaGroupingById = mfaGroupingRepository.findById(mfaGroupingNumber).get();

        if(!StringUtils.isEmpty(mfaGroupingRequest.getMfaGroupingName())){
            mfaGroupingById.setMfaGroupingName(mfaGroupingRequest.getMfaGroupingName());
        }

        if(!StringUtils.isEmpty(mfaGroupingRequest.getStatus())){
            mfaGroupingById.setStatus(mfaGroupingRequest.getStatus());
            if(mfaGroupingRequest.getStatus().equalsIgnoreCase("deleted")){
                mfaGroupingById.setDeletedBy(userPrincipleObj.getUserName());
                mfaGroupingById.setDeletedTimestamp(LocalDateTime.now());
            }
        }

        mfaGroupingById.setLastModifiedTimestamp(LocalDateTime.now());

        MFAGrouping updatedMfaGrouping = mfaGroupingRepository.save(mfaGroupingById);
        log.info("Updated successfully : ");
        return updatedMfaGrouping.getMfaGroupingNumber();
    }

    public  Specification<MFAGrouping> mfaGroupingByGaId(Long gaId) {
        return (root, query, builder) -> builder.equal(root.get("gaId"), gaId);
    }

    public Specification<MFAGrouping>  getSpecificationMfaGroupingsForGARoles(MfaGroupingSearchInput searchInput, String gaName) {
        String searchName = searchInput.getSearchName();
        Specification<MFAGrouping> mfaGroupingSpecification = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : MFAGroupingSpecificaionUtils.mfaGroupingNameLike(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : MFAGroupingSpecificaionUtils.mfaGroupingNumberEquals(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :MFAGroupingSpecificaionUtils.grantingAuthorityNameEqual(searchName.trim()))

                )
                .and(SearchUtils.checkNullOrEmptyString(gaName)
                        ? null :MFAGroupingSpecificaionUtils.grantingAuthorityNameEqual(gaName.trim()))
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null :MFAGroupingSpecificaionUtils.mfaGroupingByStatus(searchInput.getStatus().trim()));
        return mfaGroupingSpecification;
    }

    private Specification<MFAGrouping> mfaGroupingByGrantingAuthority(Long gaId) {
        return Specification.where(mfaGroupingByGa(gaId));
    }

    public  Specification<MFAGrouping> mfaGroupingByGa(Long gaId) {
        return (root, query, builder) -> builder.equal(root.get("gaId"), gaId);
    }

    private Long getGrantingAuthorityIdByName(String gaName){
        GrantingAuthority gaObj = gaRepository.findByGrantingAuthorityName(gaName);
        if(gaObj != null){
            return gaObj.getGaId();
        }
        return null;
    }
}
