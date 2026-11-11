
package com.company.insurance.repository;

import com.company.insurance.entity.ClaimHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimHeaderRepository extends JpaRepository<ClaimHeader, Long> {

    Optional<ClaimHeader> findByClaimRef(String claimRef);
}
