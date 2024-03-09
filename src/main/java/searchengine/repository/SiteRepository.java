package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;


public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByStatus(Status status);
}
