
package com.company.insurance.repository;

import com.company.insurance.entity.ClaimFraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimFraudFlagRepository
        extends JpaRepository<ClaimFraudFlag, Long> {

    List<ClaimFraudFlag> findByClaimHeaderClaimId(Long claimId);

    void deleteByClaimHeaderClaimId(Long claimId);
}
