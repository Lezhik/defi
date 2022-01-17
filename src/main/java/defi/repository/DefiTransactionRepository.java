package defi.repository;

import defi.model.DefiTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface DefiTransactionRepository extends JpaRepository<DefiTransaction, UUID> {
    @Query(
            value = "SELECT hash FROM defi_transaction WHERE hash IN (?1)",
            nativeQuery = true
    )
    public Collection<String> getExistingHashes(Iterable<String> hashes);
}
