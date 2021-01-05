package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import java.time.LocalDate;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
/**
 * 
 * LegalBasis Entity Class
 *
 */
@Builder
@Entity(name = "LEGAL_BASIS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "legal_basis_seq", sequenceName = "legal_basis_seq",
		allocationSize = 1)
public class LegalBasis {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legal_basis_seq")
	@Column(name="LEGAL_BASIS_ID")
	private Long legalBasisId;

	@OneToOne
	@JoinColumn(name = "scNumber", nullable = false)
	private SubsidyMeasure subsidyMeasure;

	@Column(name="LEGAL_BASIS_TEXT")
	private String legalBasisText;

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
