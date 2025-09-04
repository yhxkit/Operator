package com.sample.operator.app.jpa.sslCert.repository;

import com.sample.operator.app.jpa.sslCert.entity.CertSvcGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SslCertSvcGroupRepository extends JpaRepository<CertSvcGroup, Integer> {
}
