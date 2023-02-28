package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.beis.subsidy.award.transperancy.dbpublishingservice.model.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.MFAGroupingRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BulkUploadMfaAwardsService {

    @Autowired
    private AwardService awardService;

    @Autowired
    private MFAService mfaService;

    @Autowired
    private MFAGroupingRepository mfaGroupingRepository;

    private final HashMap<String, String> columnMapping = new HashMap<String, String>()
    {{
        put("SPEIA", "A");
        put("Grouping", "B");
        put("Grouping ID", "C");
        put("Full Amount", "D");
        put("Confirmation Date", "E");
        put("Public Authority", "F");
        put("Org Name", "G");
        put("Org Id Type", "H");
        put("ID Number", "I");
    }};


    /*
     * the below method validate the excel file passed in request.
     */
    public ValidationResult validateFile(MultipartFile file, String role) {

        try {

            Boolean isLatestVersion = ExcelHelper.validateMfaColumnCount(file.getInputStream());

            if(!isLatestVersion){
                ValidationResult validationResult = new ValidationResult();

                ValidationErrorResult validationErrorResult = new ValidationErrorResult();
                validationErrorResult.setRow("All");
                validationErrorResult.setColumns("All");
                validationErrorResult.setErrorMessages("The version of the template being used is not up to date. Please re-download and use the latest version.");

                validationResult.setTotalRows(1);
                validationResult.setErrorRows(1);
                validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));

                return validationResult;
            }

            // Read Excel file
            List<BulkUploadMfaAwards> bulkUploadMfaAwards = ExcelHelper.excelToMfaAwards(file.getInputStream());

            log.info("Back from Excel to awards...printed list of awards - end");

            // Validation of bulk upload template
            List<ValidationErrorResult> orgIdMissingList = validateOrgId(bulkUploadMfaAwards);

            List<ValidationErrorResult> orgIdTypeMissingList = validateOrgIdType(bulkUploadMfaAwards);

            List<ValidationErrorResult> speiaGroupingCheckList = validateSpeiaGrouping(bulkUploadMfaAwards);

            List<ValidationErrorResult> orgNameErrorList = validateOrgName(bulkUploadMfaAwards);

            List<ValidationErrorResult> publicAuthorityNameErrorList = validatePublicAuthorityName(bulkUploadMfaAwards);

            List<ValidationErrorResult> publicAuthorityNameInDbErrorList = validatePublicAuthorityNameInDb(bulkUploadMfaAwards);

            List<ValidationErrorResult> mfaAwardAmountErrorList = validateMfaAwardAmount(bulkUploadMfaAwards);

            List<ValidationErrorResult> speiaAssistanceErrorList = validateSpeiaAssistance(bulkUploadMfaAwards);

            List<ValidationErrorResult> confirmationDateErrorList = validateConfirmationDate(bulkUploadMfaAwards);

            List<ValidationErrorResult> groupingIdErrorList = validateGroupingId(bulkUploadMfaAwards);

            // Merge lists of Validation Errors
            List<ValidationErrorResult> validationErrorResultList = Stream
                    .of(speiaGroupingCheckList, orgNameErrorList,
                            orgIdTypeMissingList, orgIdMissingList,
                            publicAuthorityNameErrorList, publicAuthorityNameInDbErrorList,
                            speiaAssistanceErrorList, confirmationDateErrorList,
                            mfaAwardAmountErrorList, groupingIdErrorList)
                    .flatMap(x -> x.stream()).collect(Collectors.toList());

            log.info("Final validation errors list ...printing list of errors - start");

            ValidationResult validationResult = new ValidationResult();
            validationResult.setValidationErrorResult(validationErrorResultList);
            validationResult.setTotalRows(bulkUploadMfaAwards.size());
            validationResult.setErrorRows(validationErrorResultList.size());
            validationResult.setMessage((validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file"
                    : "No errors in Uploaded file");

            log.info("Final validation Result object ...printing validationResult - start");

            if (validationResult.getValidationErrorResult().size() == 0) {

                log.info("No validation error in bulk excel template");

                awardService.processBulkMfaAwards(bulkUploadMfaAwards, role);

                log.info("After calling process api - response = ");
                validationResult
                        .setMessage((true ? "All Awards saved in Database" : "Error while saving awards in Database"));
            }

            return validationResult;

        } catch (IOException e) {
            log.error(" Error in validationResult **** ", e);
            throw new RuntimeException("Fail to store data : " + e.getMessage());
        }

    }


    private List<ValidationErrorResult> validateGroupingId(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> groupingIdExistsRecordsList = bulkUploadMfaAwards.stream()
                .filter(
                        award -> { if(!StringUtils.isEmpty(award.getGroupingId()) && (award.getMfaSpeiaGrouping() != null)) {
                            MFAGrouping groupingId = mfaGroupingRepository.findByMfaGroupingNumber(award.getGroupingId());
                            return groupingId == null && award.getMfaSpeiaGrouping();
                        }else {
                            return false;
                        }
                        }).collect(Collectors.toList());


        List<BulkUploadMfaAwards> groupingIdWithNoErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(
                        award -> { if(award.getMfaSpeiaGrouping() != null){
                            return !award.getMfaSpeiaGrouping() && (!StringUtils.isEmpty(award.getGroupingId()));
                        }
                            return false;
                        }).collect(Collectors.toList());

        List<BulkUploadMfaAwards> groupingIdWithYesErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(
                        award -> { if(award.getMfaSpeiaGrouping() != null) {
                            return award.getMfaSpeiaGrouping() && (StringUtils.isEmpty(award.getGroupingId()));
                        }
                            return false;
                        }).collect(Collectors.toList());



        List<ValidationErrorResult> validationGroupingIdResultList = new ArrayList<>();
        validationGroupingIdResultList = groupingIdWithNoErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Grouping ID"),
                        "If 'MFA/SPEIA Grouping' is 'No', you must not provide an Grouping ID"))
                .collect(Collectors.toList());

        validationGroupingIdResultList.addAll(
                groupingIdWithYesErrorRecordsList.stream()
                        .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Grouping ID"),
                                "If 'MFA/SPEIA Grouping' is 'Yes', you must provide an Grouping ID"))
                        .collect(Collectors.toList())
        );

        validationGroupingIdResultList.addAll(
                groupingIdExistsRecordsList.stream()
                        .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Grouping ID"),
                                "The Grouping ID doesnt exist"))
                        .collect(Collectors.toList())
        );

        return validationGroupingIdResultList;
    }



    private List<ValidationErrorResult> validateSpeiaGrouping(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {



        List<BulkUploadMfaAwards> speiaGroupingErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getMfaSpeiaGrouping() == null)))
                .collect(Collectors.toList());

        List<ValidationErrorResult> validationSpeiaGroupingErrorListResultList = new ArrayList<>();
        validationSpeiaGroupingErrorListResultList = speiaGroupingErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Grouping"),
                        "You must select if the award has MFA/SPEIA grouping or not."))
                .collect(Collectors.toList());

        return validationSpeiaGroupingErrorListResultList;
    }



    private List<ValidationErrorResult> validateMfaAwardAmount(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<ValidationErrorResult> validationMfaAwardAmountErrorResultList = new ArrayList<>();

        List<BulkUploadMfaAwards> subsidyAmountFormatErrorRecordsList = bulkUploadMfaAwards.stream().filter(
                award -> !StringUtils.isEmpty(award.getAwardFullAmount()) && !ExcelHelper.isNumeric(award.getAwardFullAmount()))
                .collect(Collectors.toList());

        List<BulkUploadMfaAwards> subsidyAmountEmptyErrorRecordsList = bulkUploadMfaAwards.stream().filter(
                        award -> StringUtils.isEmpty(award.getAwardFullAmount()))
                .collect(Collectors.toList());

            List<BulkUploadMfaAwards> subsidyAmountNegativeErrorRecordsList = bulkUploadMfaAwards.stream().filter(
                    award -> { if(ExcelHelper.isNumeric(award.getAwardFullAmount())){
                        return (Float.parseFloat(award.getAwardFullAmount()) < 0) || (Float.parseFloat(award.getAwardFullAmount()) % 1 != 0);
                    }
                    return false;
                    }).collect(Collectors.toList());

        if(subsidyAmountFormatErrorRecordsList.size()>0) {
            validationMfaAwardAmountErrorResultList.addAll(subsidyAmountFormatErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Amount"),
                            "MFA award amount should only contain numeric characters (0-9)."))
                    .collect(Collectors.toList()));
        }

        if(subsidyAmountNegativeErrorRecordsList.size()>0) {
            validationMfaAwardAmountErrorResultList.addAll(subsidyAmountNegativeErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Amount"),
                            "MFA award amount should only contain positive whole numbers."))
                    .collect(Collectors.toList()));
        }

        if(subsidyAmountEmptyErrorRecordsList.size()>0) {
            validationMfaAwardAmountErrorResultList.addAll(subsidyAmountEmptyErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Amount"),
                            "You must provide a MFA Award full amount."))
                    .collect(Collectors.toList()));
        }

        return validationMfaAwardAmountErrorResultList;
    }


    private List<ValidationErrorResult> validateSpeiaAssistance(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> speiaAssistanceErrorRecordsList = bulkUploadMfaAwards.stream().filter(
                        award -> ((award.getSpeiaAward() == null)))
                .collect(Collectors.toList());

        List<ValidationErrorResult> validationSpeiaAssistanceErrorListResultList = new ArrayList<>();
        validationSpeiaAssistanceErrorListResultList = speiaAssistanceErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("SPEIA"),
                        "You must select if the award has SPEI Assistance or not."))
                .collect(Collectors.toList());

        return validationSpeiaAssistanceErrorListResultList;
    }



    private List<ValidationErrorResult> validateOrgIdType(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> orgIdTypeErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getOrgIdType() == null))).collect(Collectors.toList());

        List<ValidationErrorResult> validationOrgIdTypeResultList = new ArrayList<>();
        validationOrgIdTypeResultList = orgIdTypeErrorRecordsList.stream().map(
                        award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Org Id Type"), "You must select an ID type for the recipient organisation."))
                .collect(Collectors.toList());

        return validationOrgIdTypeResultList;
    }

    private List<ValidationErrorResult> validateOrgName(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> orgNameMissingErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getOrgName()==null  || StringUtils.isEmpty(award.getOrgName())))).collect(Collectors.toList());

        List<ValidationErrorResult> validationOrgNameResultList = new ArrayList<>();
        validationOrgNameResultList = orgNameMissingErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Org Name"),
                        "Organisation name field is Mandatory"))
                .collect(Collectors.toList());

        List<BulkUploadMfaAwards> orgNameErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getOrgName()!=null && award.getOrgName().length() > 255))).collect(Collectors.toList());


        validationOrgNameResultList .addAll( orgNameErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Org Name"),
                        "Organisation name is too long, it should be 255 characters or fewer"))
                .collect(Collectors.toList()));

        return validationOrgNameResultList;
    }

    private List<ValidationErrorResult> validateConfirmationDate(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> confirmationDateMissingErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getConfirmationDate()==null))).collect(Collectors.toList());

        List<ValidationErrorResult> validationConfirmationDateResultList = new ArrayList<>();
        validationConfirmationDateResultList = confirmationDateMissingErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Confirmation Date"),
                        "You must enter a valid confirmation date of the subsidy scheme and it must be in the following format: DD-MM-YYYY."))
                .collect(Collectors.toList());

        return validationConfirmationDateResultList;
    }

    private List<ValidationErrorResult> validatePublicAuthorityName(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> validateGrantingAuthorityNameErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> ((award.getPublicAuthority()!=null && award.getPublicAuthority().length() > 255))).collect(Collectors.toList());

        List<ValidationErrorResult> validationPublicAuthorityResultList = new ArrayList<>();
        validationPublicAuthorityResultList = validateGrantingAuthorityNameErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public Authority"),
                        "The public authority name must be less than 255 characters."))
                .collect(Collectors.toList());

        List<BulkUploadMfaAwards> validatePublicAuthorityMissingErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> (award.getPublicAuthority()==null)).collect(Collectors.toList());
        if(validatePublicAuthorityMissingErrorRecordsList.size()>0) {
            validationPublicAuthorityResultList = validatePublicAuthorityMissingErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public Authority"),
                            "You must enter the name of the public authority."))
                    .collect(Collectors.toList());
        }

        return validationPublicAuthorityResultList;
    }

    private List<ValidationErrorResult> validatePublicAuthorityNameInDb(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        log.info("Calling validatePublicAuthorityNameInDb... - start");

        List<GrantingAuthority> publicAuthorityList = awardService.getAllGrantingAuthorities();

        List<String> publicAuthorityNamesList = publicAuthorityList.stream()
                .map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());

        log.info("Public Authority - String list size " + publicAuthorityNamesList.size());

        List<BulkUploadMfaAwards> publicAuthorityNameErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getPublicAuthority() != null
                        && !publicAuthorityNamesList.contains(award.getPublicAuthority()))
                .collect(Collectors.toList());

        List<ValidationErrorResult> validationPublicAuthorityNameResultList = new ArrayList<>();
        validationPublicAuthorityNameResultList = publicAuthorityNameErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public Authority"),
                        "You must enter the name of the public authority."))
                .collect(Collectors.toList());

        return validationPublicAuthorityNameResultList;
    }

    private List<ValidationErrorResult> validateOrgId(List<BulkUploadMfaAwards> bulkUploadMfaAwards) {

        List<BulkUploadMfaAwards> orgIdErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getIdNumber()!=null && award.getIdNumber().length() > 255).collect(Collectors.toList());

        List<BulkUploadMfaAwards> orgIdMissingRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getIdNumber()==null || StringUtils.isEmpty(award.getIdNumber())).collect(Collectors.toList());


        List<ValidationErrorResult> validationOrgIdResultList = new ArrayList<>();
        validationOrgIdResultList = orgIdErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                        "National ID  must be less than  or 10 characters"))
                .collect(Collectors.toList());

        if(orgIdMissingRecordsList.size()>0) {
            validationOrgIdResultList = orgIdMissingRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                            "National ID  field is Mandatory."))
                    .collect(Collectors.toList());
        }

        List<BulkUploadMfaAwards> orgIdVATErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getOrgIdType()!=null && award.getOrgIdType().equalsIgnoreCase("VAT Number") && (award.getIdNumber()!=null && (award.getIdNumber().length() > 255))).collect(Collectors.toList());

        if(orgIdVATErrorRecordsList.size()>0) {
            validationOrgIdResultList.addAll(orgIdVATErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                            "The VAT number must be 9 digits."))
                    .collect(Collectors.toList()));
        }

        List<BulkUploadMfaAwards> orgIdUTRErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getOrgIdType()!=null && award.getOrgIdType().equalsIgnoreCase("UTR Number") && (award.getIdNumber()!=null && (award.getIdNumber().length() > 255))).collect(Collectors.toList());

        if(orgIdUTRErrorRecordsList.size()>0) {
            validationOrgIdResultList.addAll(orgIdUTRErrorRecordsList.stream()
                    .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                            "The UTR number must be 10 digits."))
                    .collect(Collectors.toList()));
        }

        List<BulkUploadMfaAwards> orgIdCharityErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getOrgIdType()!=null && award.getOrgIdType()
                        .equalsIgnoreCase("Charity Number") && (award.getIdNumber()!=null &&
                        (award.getIdNumber().length() > 255))).collect(Collectors.toList());

        validationOrgIdResultList.addAll(orgIdCharityErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                        "The charity commission number must be 8 digits. This may include a dash (-) before the last digit."))
                .collect(Collectors.toList()));


        List<BulkUploadMfaAwards> orgIdCompanyNumberFormatErrorRecordsList = bulkUploadMfaAwards.stream()
                .filter(award -> award.getOrgIdType()!=null && award.getOrgIdType().equalsIgnoreCase("Company Registration Number")
                        && (award.getIdNumber()!=null &&  (award.getIdNumber().length() > 255))).collect(Collectors.toList());

        validationOrgIdResultList.addAll(orgIdCompanyNumberFormatErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Number"),
                        "The company number must be in one of the following formats:8 digits 2 letters, followed by 6 digits"))
                .collect(Collectors.toList()));

        return validationOrgIdResultList;

    }

    private boolean validateCompanyNumber(String companyNumber) {

        int charCount = 0;
        int degitCount = 0;
        boolean isFormat = true;
        int firstOccurence = -1;

        if(companyNumber.length()!=8) {
            return false;
        }
        for (int i = 0; i < companyNumber.length(); i++) {
            if (Character.isLetter(companyNumber.charAt(i))) {
                charCount++;
                if (firstOccurence < 0) {
                    firstOccurence = i;

                } else {
                    if (i - firstOccurence > 1) {
                        isFormat = false;
                    }
                }
            } else if (Character.isDigit(companyNumber.charAt(i))) {
                degitCount++;
            }
        }

        if ((charCount > 0) && (!isFormat || (charCount > 2 || degitCount > 6))) {
            return false;
        } else if (charCount == 0 && degitCount == 8) {
            return true;
        } else {
            return true;
        }
    }

}

