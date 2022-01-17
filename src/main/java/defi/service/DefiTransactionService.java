package defi.service;

import defi.model.DefiTransaction;
import defi.repository.DefiTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class DefiTransactionService {
    @Autowired
    DefiTransactionRepository repo;

    public List<DefiTransaction> save(Iterable<DefiTransaction> transactions) {
        return repo.saveAllAndFlush(transactions);
    }

    public Collection<String> getExistingHashes(Iterable<String> hashes) {
        return repo.getExistingHashes(hashes);
    }
}
