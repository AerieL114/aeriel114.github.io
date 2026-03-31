package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCartId(String cartId);

    Optional<Reservation> findByCartIdAndSkuCode(String cartId, String skuCode);
}
