package ecotrack.backend.repositories;

import ecotrack.backend.models.entitys.CarbonTotal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarbonTotalRepository extends JpaRepository<CarbonTotal, Long> {

    Optional<CarbonTotal> findByUserId(Long userId);

}
