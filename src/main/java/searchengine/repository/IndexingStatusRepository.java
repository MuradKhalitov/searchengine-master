package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexingStatus;

@Repository
public interface IndexingStatusRepository extends JpaRepository<IndexingStatus, Long> {
}
