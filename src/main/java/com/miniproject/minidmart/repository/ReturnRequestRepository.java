package com.miniproject.minidmart.repository;

import com.miniproject.minidmart.entity.ReturnRequest;
import com.miniproject.minidmart.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnStatus status);
}