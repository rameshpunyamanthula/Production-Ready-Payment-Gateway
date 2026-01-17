package com.gateway.repositories;

import com.gateway.models.IdempotencyKey;
import com.gateway.models.IdempotencyKey.IdempotencyKeyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, IdempotencyKeyId> {
}
