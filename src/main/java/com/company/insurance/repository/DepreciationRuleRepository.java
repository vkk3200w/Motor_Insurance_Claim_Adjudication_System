
package com.company.insurance.repository;

import com.company.insurance.entity.DepreciationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepreciationRuleRepository
        extends JpaRepository<DepreciationRule, Integer> {

    List<DepreciationRule> findByCategory(String category);
}
