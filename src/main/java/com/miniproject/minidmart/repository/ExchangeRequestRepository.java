package com.miniproject.minidmart.repository;

import com.miniproject.minidmart.entity.ExchangeRequest;
import com.miniproject.minidmart.enums.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    List<ExchangeRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ExchangeRequest> findByStatusOrderByCreatedAtDesc(ExchangeStatus status);
}