package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * Beneficiary Entity class
 *
 */
@Builder
@Entity(name = "BENEFICIARY")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "beneficiary_read_seq",sequenceName="beneficiary_read_seq",allocationSize=1)
public class Beneficiary {

	
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO,generator="beneficiary_read_seq")
	@Column(name="BENEFICIARY_ID")
	private Long beneficiaryId;
		
	@OneToMany(mappedBy="beneficiary")
	@ToString.Exclude
	@JsonIgnore
	private List<Award> awards;
		
	@Column(name = "BENEFICIARY_NAME")
	private String beneficiaryName;
		
	@Column(name = "BENEFICIARY_TYPE")
	private String beneficiaryType;
				
	@Column(name = "NATIONAL_ID")
	private String nationalId;
		
	@Column(name = "NATIONAL_ID_TYPE")
	private String nationalIdType;
		
	@Column(name = "SIC_CODE")
	private String sicCode;
		
	@Column(name = "SIZE_OF_ORG")
	private String orgSize;
		
	@Column(name = "CREATED_BY")
	private String createdBy;
		
	@Column(name = "APPROVED_BY")
	private String approvedBy;
		
	@Column(name = "STATUS")
	private String status;
		
	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDate createdTimestamp;
		
	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private LocalDate lastModifiedTimestamp;
}
