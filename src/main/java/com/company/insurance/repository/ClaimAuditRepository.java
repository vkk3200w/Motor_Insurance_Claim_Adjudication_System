
package com.company.insurance.repository;

import com.company.insurance.entity.ClaimAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimAuditRepository extends JpaRepository<ClaimAudit, Long> {

    List<ClaimAudit> findByClaimHeaderClaimId(Long claimId);
}
