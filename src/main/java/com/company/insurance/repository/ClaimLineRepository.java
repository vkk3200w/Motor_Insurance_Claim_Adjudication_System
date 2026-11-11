
package com.company.insurance.repository;

import com.company.insurance.entity.ClaimLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimLineRepository extends JpaRepository<ClaimLine, Long> {

    List<ClaimLine> findByClaimHeaderClaimId(Long claimId);
    void deleteByClaimHeaderClaimId(Long claimId);
}
