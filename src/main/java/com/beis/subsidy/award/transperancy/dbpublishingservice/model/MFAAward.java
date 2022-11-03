package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity(name = "MFA_AWARD")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MFAAward {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mfa_award_read_seq")
    @Column(name="MFA_AWARD_NUMBER")
    private Long mfaAwardNumber;

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

    @Column(name = "GA_ID")
    private Long gaId;

    @Column(name="MFA_GROUPING_NUMBER")
    private String mfaGroupingNumber;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "STATUS")
    private String status;

    @CreationTimestamp
    @Column(name = "CREATED_TIMESTAMP")
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    @Column(name = "LAST_MODIFIED_TIMESTAMP")
    private LocalDateTime lastModifiedTimestamp;

    @Column(name = "DELETED_BY")
    private String deletedBy;

    @Column(name = "DELETED_TIMESTAMP")
    private LocalDateTime deletedTimestamp;

    @Column(name = "CONFIRMATION_DATE")
    private LocalDate confirmationDate;
}
