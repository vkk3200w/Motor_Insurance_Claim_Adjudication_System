
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CLAIM_AUDIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private ClaimHeader claimHeader;

    @Column(name = "event_type", nullable = false, length = 15)
    private String eventType;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
