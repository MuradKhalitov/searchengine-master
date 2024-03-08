package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "indexing")
public class Indexing {
    private boolean indexingInProgress;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    @Column(name = "ranking", nullable = false)
    private float ranking;
public Indexing(){}
    public Indexing(Page page, Lemma lemma) {
        this.page = page;
        this.lemma = lemma;
        this.ranking = calculateRank(page, lemma);
    }

    private float calculateRank(Page page, Lemma lemma) {
        // Предположим, что чем чаще лемма встречается на странице, тем выше рейтинг
        // Можно использовать другие алгоритмы, например, TF-IDF или машинное обучение
        int lemmaFrequencyOnPage = countLemmaFrequencyOnPage(page, lemma);
        return (float) lemmaFrequencyOnPage / page.getContent().split(" ").length;
    }

    private int countLemmaFrequencyOnPage(Page page, Lemma lemma) {
        String content = page.getContent().toLowerCase(); // Приводим к нижнему регистру для учета всех вариантов
        String[] words = content.split("\\s+"); // Разбиваем контент на слова
        int frequency = 0;
        for (String word : words) {
            // Учитываем только точные совпадения леммы
            if (word.equals(lemma.getLemma().toLowerCase())) {
                frequency++;
            }
        }
        return frequency;
    }
}
