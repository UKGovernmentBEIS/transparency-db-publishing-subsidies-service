package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class AwardService {

	private static final String CONSTANT_SYSTEM = "SYSTEM";
	
	@Autowired
	private AwardRepository awardRepository;
	
	@Autowired
	private BeneficiaryRepository beneficiaryRepository;

	@Autowired
	private AdminProgramRepository adminProgramRepository;
	
	@Autowired
	private GrantingAuthorityRepository gaRepository;
	
	@Autowired
	private SubsidyMeasureRepository smRepository;

	@Value("${loggingComponentName}")
	private String loggingComponentName;
	
	private Date convertToDate(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		Date date = null ;
		try {
			date = formatter.parse(incomingDate);
		} catch (ParseException e) {
		  log.error("{}:: date error parse issue{}",loggingComponentName,e);
		}
		
		return date;
	}
	
	private Date convertToDateSingleUpload(String incomingDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		Date date = null ;
		try {
			date = formatter.parse(incomingDate);
		} catch (ParseException e) {
			log.error("{}:: date error parse issue{}",loggingComponentName,e);
		}
		
		return date;
	}

	@Transactional
	public List<Award> saveAwards(List<Award> awards) {
		
		List<Beneficiary> beneficiaries = beneficiaryRepository.saveAll(
					awards.stream()
					.map(award -> award.getBeneficiary()).collect(Collectors.toList()));
		
		return awardRepository.saveAll(awards);
	}
	
	@Transactional
	public List<Award> processBulkAwards(List<BulkUploadAwards> bulkAwards, String role) {
		try {
		log.info("{} ::inside process Bulk Awards db",loggingComponentName);
		List<Beneficiary> beneficiaries = bulkAwards.stream()
			.map( award -> {
				Beneficiary beneficiary = new Beneficiary();
				beneficiary.setBeneficiaryName(award.getBeneficiaryName());
				beneficiary.setBeneficiaryType("Individual");
				beneficiary.setNationalId(award.getNationalId());
				beneficiary.setNationalIdType(award.getNationalIdType());
				beneficiary.setOrgSize(award.getOrgSize());
				beneficiary.setCreatedBy("SYSTEM");
				beneficiary.setApprovedBy("SYSTEM");
				beneficiary.setStatus("DRAFT");
				beneficiary.setSicCode("14455");
				beneficiary.setCreatedTimestamp(LocalDate.now());
				beneficiary.setLastModifiedTimestamp(LocalDate.now());
				return beneficiary;
				})
			.collect(Collectors.toList());
		
		beneficiaryRepository.saveAll(beneficiaries);


		List<Award> awards = bulkAwards.stream()
				.map( bulkaward -> new Award(null, getBeneficiaryDetails(bulkaward, beneficiaries), getGrantingAuthority(bulkaward), getSubsidyMeasure(bulkaward), bulkaward.getSubsidyAmountRange(), 
						( (bulkaward.getSubsidyAmountExact() != null) ? new BigDecimal(bulkaward.getSubsidyAmountExact()) : BigDecimal.ZERO),  
						((bulkaward.getSubsidyObjective().equalsIgnoreCase("Other"))? "Other - "+bulkaward.getSubsidyObjectiveOther():bulkaward.getSubsidyObjective()), bulkaward.getGoodsOrServices(),
						convertToDate(bulkaward.getLegalGrantingDate()),
						addPublishedDate(role),
						bulkaward.getSpendingRegion(), 
						((bulkaward.getSubsidyInstrument().equalsIgnoreCase("Other"))? "Other - "+bulkaward.getSubsidyInstrumentOther():bulkaward.getSubsidyInstrument()),
						bulkaward.getSpendingSector(),
						"SYSTEM", 
						"SYSTEM",
						addAwardStatus(role),null,LocalDate.now(), LocalDate.now(),
						StringUtils.capitalize(StringUtils.lowerCase(bulkaward.getStandaloneAward())),
						bulkaward.getSubsidyDescription(),
						getAdminProgram(bulkaward))
				
					)
				.collect(Collectors.toList());
				
		List<Award> savedAwards = awardRepository.saveAll(awards);
		log.info("End process Bulk Awards db");
				
		return savedAwards;
		} catch(Exception serviceException) {
			log.error("serviceException occured::" , serviceException);
			return null;
		}
	}

	private AdminProgram getAdminProgram(BulkUploadAwards bulkAward) {
		return adminProgramRepository.findById(bulkAward.getAdminProgramNumber()).orElse(null);
	}

	private String addAwardStatus(String role) {
		String awardStatus = "Published";
		if ("Granting Authority Encoder".equals(role.trim())) {
			awardStatus = "Awaiting Approval";
		}
		return awardStatus;
	}

	private Date addPublishedDate(String role) {
		String publishDateStr = "01-01-1970";
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		if (!"Granting Authority Encoder".equals(role.trim())) {
			Date date = new Date();
			publishDateStr = formatter.format(date);
		}
		return convertToDateSingleUpload(publishDateStr);
	}

	@Transactional
	public Award createAward(SingleAward award, String role) {
		try {
			log.info("inside process Bulk Awards db");
			String awardStatus = "Published";
			Beneficiary beneficiary = new Beneficiary();
			beneficiary.setBeneficiaryName(award.getBeneficiaryName());
			beneficiary.setBeneficiaryType("Individual");
			beneficiary.setNationalId(award.getNationalId());
			beneficiary.setNationalIdType(award.getNationalIdType());
			beneficiary.setOrgSize(award.getOrgSize());
			beneficiary.setCreatedBy("SYSTEM");
			beneficiary.setApprovedBy("SYSTEM");
			beneficiary.setStatus("DRAFT");

			beneficiary.setSicCode("14455");
			beneficiary.setCreatedTimestamp(LocalDate.now());
			beneficiary.setLastModifiedTimestamp(LocalDate.now());

			beneficiaryRepository.save(beneficiary);

			BulkUploadAwards tempAward = new BulkUploadAwards();
			tempAward.setGrantingAuthorityName(award.getGrantingAuthorityName());
			tempAward.setSubsidyControlTitle(award.getSubsidyControlTitle());
			tempAward.setSubsidyControlNumber(award.getSubsidyControlNumber());

			if ("Granting Authority Encoder".equals(role.trim())) {
				awardStatus = "Awaiting Approval";
			}

			AdminProgram adminProgram = adminProgramRepository.findById(award.getAdminProgramNumber()).orElse(null);

			Award saveAward = new Award(null, beneficiary, getGrantingAuthority(tempAward),
					getSubsidyMeasure(tempAward), award.getSubsidyAmountRange(),
					((award.getSubsidyAmountExact() != null) ? new BigDecimal(award.getSubsidyAmountExact())
							: BigDecimal.ZERO),
					((award.getSubsidyObjective().equalsIgnoreCase("Other")) ? "Other - "+award.getSubsidyObjectiveOther()
							: award.getSubsidyObjective()),
					award.getGoodsOrServices(), convertToDateSingleUpload(award.getLegalGrantingDate()),
					addPublishedDate(role), award.getSpendingRegion(),
					((award.getSubsidyInstrument().equalsIgnoreCase("Other")) ? "Other - "+award.getSubsidyInstrumentOther()
							: award.getSubsidyInstrument()),
					award.getSpendingSector(), "SYSTEM", "SYSTEM", awardStatus, null,LocalDate.now(), LocalDate.now(), award.getStandaloneAward(), award.getSubsidyAwardDescription(),
					adminProgram);

			Award savedAwards = awardRepository.save(saveAward);
			log.info("{} :: End process Bulk Awards db");

			return savedAwards;
		} catch (Exception ex) {
			log.error("{} :: Award is failed to save ::{} and exception {}",ex.getMessage(),ex);
			return null;
		}
	}
	
	@Transactional
	public Award updateAward(Long awardNumber,SingleAward awardUpdateRequest, String role) {

		try {
			log.info("inside updateAward db");
			Award award = awardRepository.findByAwardNumber(awardNumber);
			if (Objects.isNull(award)) {

				throw new Exception("Award details not found::" + awardNumber);
			}

			award.setStatus(awardUpdateRequest.getStatus());

			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			if (formatter.format(award.getPublishedAwardDate()).equals("01-01-1970")) {
				Date date = new Date();
				String publishDate = formatter.format(date);
				award.setPublishedAwardDate(convertToDateSingleUpload(publishDate));
			}

			award.setLastModifiedTimestamp(LocalDate.now());
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyAmountExact())) {
				award.setSubsidyFullAmountExact(new BigDecimal(awardUpdateRequest.getSubsidyAmountExact()));
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyAmountRange())) {
				award.setSubsidyFullAmountRange(awardUpdateRequest.getSubsidyAmountRange());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSpendingRegion())) {
				award.setSpendingRegion(awardUpdateRequest.getSpendingRegion());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSpendingSector())) {
				award.setSpendingSector(awardUpdateRequest.getSpendingSector());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getGoodsOrServices())) {
				award.setGoodsServicesFilter(awardUpdateRequest.getGoodsOrServices());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyObjective())) {
				award.setSubsidyObjective(awardUpdateRequest.getSubsidyObjective());
			} else if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyObjectiveOther())) {
				award.setSubsidyObjective(awardUpdateRequest.getSubsidyObjectiveOther());
			}

			if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyInstrument())) {
				award.setSubsidyInstrument(awardUpdateRequest.getSubsidyInstrument().trim());
			} else if(!StringUtils.isEmpty(awardUpdateRequest.getSubsidyInstrumentOther())){
				award.setSubsidyInstrument(awardUpdateRequest.getSubsidyInstrumentOther().trim());
			}

			if (!StringUtils.isEmpty(awardUpdateRequest.getLegalGrantingDate())) {
				award.setLegalGrantingDate(convertToDateSingleUpload(awardUpdateRequest.getLegalGrantingDate().trim()));
			}

			if(!StringUtils.isEmpty(awardUpdateRequest.getReason())){
				award.setReason(awardUpdateRequest.getReason());
			}

			if(!awardUpdateRequest.getStandaloneAward().equalsIgnoreCase("yes")) {
				SubsidyMeasure measure = award.getSubsidyMeasure();

				if (!StringUtils.isEmpty(awardUpdateRequest.getSubsidyControlTitle())) {
					measure.setSubsidyMeasureTitle(awardUpdateRequest.getSubsidyControlTitle().trim());
				}
				award.setSubsidyMeasure(measure);
			}else if(awardUpdateRequest.getStandaloneAward().equalsIgnoreCase("yes")){
				if(!StringUtils.isEmpty(awardUpdateRequest.getSubsidyAwardDescription())){
					award.setSubsidyAwardDescription(awardUpdateRequest.getSubsidyAwardDescription().trim());
				}
			}
			Beneficiary beneficiaryDtls = award.getBeneficiary();
			if (!StringUtils.isEmpty(awardUpdateRequest.getNationalId())) {
				beneficiaryDtls.setNationalId(awardUpdateRequest.getNationalId().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getNationalIdType())) {
				beneficiaryDtls.setNationalIdType(awardUpdateRequest.getNationalIdType().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getBeneficiaryName())) {
				beneficiaryDtls.setBeneficiaryName(awardUpdateRequest.getBeneficiaryName().trim());
			}
			if (!StringUtils.isEmpty(awardUpdateRequest.getOrgSize())) {
				beneficiaryDtls.setOrgSize(awardUpdateRequest.getOrgSize().trim());
			}
			award.setBeneficiary(beneficiaryDtls);

			GrantingAuthority grantingAuthority = award.getGrantingAuthority();
			if (!StringUtils.isEmpty(awardUpdateRequest.getGrantingAuthorityName())) {
				grantingAuthority.setGrantingAuthorityName(awardUpdateRequest.getGrantingAuthorityName().trim());
				award.setGrantingAuthority(grantingAuthority);
			}

			if(!StringUtils.isEmpty(awardUpdateRequest.getAdminProgramNumber())){
				award.setAdminProgram(adminProgramRepository.findById(awardUpdateRequest.getAdminProgramNumber()).orElse(null));
			}

			Award savedAwards = awardRepository.save(award);
			log.info("{} ::End of update Award info in db");
			return savedAwards;
		} catch (Exception serviceException) {
			log.error("serviceException occurred::", serviceException);
			return null;
		}
	}

	private Beneficiary getBeneficiaryDetails(BulkUploadAwards bulkAward, List<Beneficiary> beneficiaries) {
		
		Optional<Beneficiary> benfOptional = beneficiaries.stream().filter(bene -> bene.getBeneficiaryName().equals(bulkAward.getBeneficiaryName())).findAny();
		
		return ( (benfOptional != null) ? benfOptional.get() : null );
	}


	
	private SubsidyMeasure getSubsidyMeasure(BulkUploadAwards award) {

		log.info("Inside getSubsidyControlId..." + award.getSubsidyControlTitle());
		List<SubsidyMeasure> smList = smRepository.findAll();
		Optional<SubsidyMeasure> smOptional = null;
		if (!StringUtils.isEmpty(award.getSubsidyControlNumber())) {
			smOptional = smList.stream().filter(sm -> sm.getScNumber().equals(award.getSubsidyControlNumber()))
					.findAny();

		} else if (!StringUtils.isEmpty(award.getSubsidyControlTitle())){
			log.info("inside else title");
			smOptional = smList.stream()
					.filter(sm -> sm.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle())).findAny();
		}

		return ((smOptional != null) ? smOptional.get() : null);
	}

	
	
	private GrantingAuthority getGrantingAuthority(BulkUploadAwards award) {
		
		log.info("Inside getGrantingAuthority...");

		List<GrantingAuthority> gaList = gaRepository.findAll();
		
		Optional<GrantingAuthority> gaOptional = gaList.stream().filter(ga -> ga.getGrantingAuthorityName().equals(award.getGrantingAuthorityName())).findAny();
		
		log.info("Returning from getGrantingAuthorityId.. = " + gaOptional.get().getGaId());
		return ((gaOptional != null) ? gaOptional.get() : null);
	}

	public List<Award> getAwardsByScNumber(String scNumber){
		SubsidyMeasure sm = smRepository.findByScNumber(scNumber);
		return awardRepository.findBySubsidyMeasure(sm);
	}

	public List<SubsidyMeasure> getAllSubsidyMeasures() {
		return smRepository.findAll();
	}
	
	public List<GrantingAuthority> getAllGrantingAuthorities() {
		return gaRepository.findAll();
	}
}
