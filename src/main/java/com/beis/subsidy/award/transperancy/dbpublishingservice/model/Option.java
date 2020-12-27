package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.LocalDate;
import java.util.Date;

/**
 * 
 * OPTION Entity Class
 *
 */
@Builder
@Entity(name = "OPTION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "option_seq", sequenceName = "option_seq",
		allocationSize = 1)
public class Option {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "option_seq")
	@Column(name="OPTION_ID")
	private Long optionId;

	@Column(name = "OPTION_NAME")
	private String optionName;

	@Column(name = "OPTION_VALUE")
	@NaturalId
	private String optionValue;
	
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
