package searchengine.services.search;

import lombok.Data;

@Data
public class LemmaFrequency {
    private String lemma;
    private float frequency;
}
