package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity(name = "MFA_AWARD")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MFAAward {

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "GA_ID", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @JsonIgnore
    private GrantingAuthority grantingAuthority;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "MFA_GROUPING_NUMBER", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @JsonIgnore
    private MFAGrouping mfaGrouping;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mfa_award_read_seq")
    @SequenceGenerator(name = "mfa_award_read_seq", sequenceName = "mfa_award_read_seq",
            allocationSize = 1)
    @Column(name="MFA_AWARD_NUMBER")
    private Long mfaAwardNumber;

    @Column(name = "IS_SPEI")
    private boolean isSPEI;

    @Column(name = "HAS_MFA_GROUPING")
    private boolean mfaGroupingPresent;

    @Column(name="MFA_GROUPING_NUMBER")
    private String mfaGroupingNumber;

    @Column(name = "AWARD_AMOUNT")
    private BigDecimal awardAmount;

    @Column(name = "CONFIRMATION_DATE")
    private LocalDate confirmationDate;

    @Column(name = "GA_ID")
    private Long gaId;

    @Column(name = "RECIPIENT_NAME")
    private String recipientName;

    @Column(name = "RECIPIENT_ID_TYPE")
    private String recipientIdType;

    @Column(name = "RECIPIENT_ID")
    private String recipientId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "APPROVED_BY")
    private String approvedBy;

    @Column(name = "REASON")
    private String reason;

    @CreationTimestamp
    @Column(name = "CREATED_TIMESTAMP")
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    @Column(name = "LAST_MODIFIED_TIMESTAMP")
    private LocalDateTime lastModifiedTimestamp;
}
