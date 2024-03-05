package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexing;

@Repository
public interface IndexingRepository extends JpaRepository<Indexing, Long> {
    boolean existsByIndexingInProgress(boolean indexingInProgress);
}


