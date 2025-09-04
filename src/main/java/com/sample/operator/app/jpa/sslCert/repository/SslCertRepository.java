package com.sample.operator.app.jpa.sslCert.repository;

import com.sample.operator.app.jpa.sslCert.entity.SslCert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SslCertRepository extends JpaRepository<SslCert, Integer> {
}
