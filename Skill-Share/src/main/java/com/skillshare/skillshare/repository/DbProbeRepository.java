package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.entity.DbProbe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbProbeRepository extends JpaRepository<DbProbe, Long> {
}