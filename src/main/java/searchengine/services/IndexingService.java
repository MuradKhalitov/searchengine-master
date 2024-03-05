package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Indexing;
import searchengine.repository.IndexingRepository;

@Service
public class IndexingService {
    @Autowired
    private IndexingRepository indexingRepository;

    public boolean startIndexing() {
        if (indexingRepository.existsByIndexingInProgress(true)) {
            return false;
        }

        // Ваш код для запуска индексации

        Indexing indexing = new Indexing();
        indexing.setIndexingInProgress(true);
        indexingRepository.save(indexing);
        return true;
    }
}

