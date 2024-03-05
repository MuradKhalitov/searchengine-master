package searchengine.repository;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
//    @PersistenceContext
//    EntityManager entityManager = null;
//    public default Lemma findByLemma(String lemma) {
//        return entityManager.createQuery("SELECT l FROM Lemma l WHERE l.lemma = :lemma", Lemma.class)
//                .setParameter("lemma", lemma)
//                .getResultList()
//                .stream()
//                .findFirst()
//                .orElse(null);
   // }
}
