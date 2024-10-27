package com.rudniev.hackentesttask.repository;

import com.rudniev.hackentesttask.entity.LoadState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<LoadState, Integer> {
}
