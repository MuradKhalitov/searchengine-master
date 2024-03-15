package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import searchengine.model.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemmatizer {

    public Map<String, Integer> lemmatizeText(String text) throws IOException {

        // Создаем экземпляр класса для морфологического анализа русского языка
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        // Удаляем все знаки препинания и символы, оставляя только слова
        text = text.replaceAll("[^а-яА-Я\\s]", "")
                // Замена последовательностей пробелов на одиночные пробелы
                .replaceAll("(^\\s+|\\s+$|\\s+\\s+)", " ")
                .replaceAll("(^\\s+)", "");

        // Разделяем текст на отдельные слова
        String[] words = text.split("\\s+");

        // Создаем HashMap для хранения лемм и их количества упоминаний с сохранением порядка
        Map<String, Integer> lemmaCountMap = new HashMap<>();

        // Проходимся по каждому слову в тексте
        for (String word : words) {
            // Получаем базовые формы слова
            List<String> baseForms = luceneMorph.getNormalForms(word);

            // Проходимся по каждой базовой форме слова
            for (String baseForm : baseForms) {
                // Добавляем базовую форму слова в карту и увеличиваем счетчик упоминаний
                lemmaCountMap.put(baseForm, lemmaCountMap.getOrDefault(baseForm, 0) + 1);
            }
        }

        return lemmaCountMap;
    }
    public static String cleanHtml(String html) {
        // Парсинг HTML с использованием Jsoup
        Document doc = Jsoup.parse(html);

        // Удаление всех HTML-тегов, кроме разрешенных
        Safelist Whitelist = null;

        String cleanedHtml = Jsoup.clean(doc.body().html(), Whitelist.simpleText());

        return cleanedHtml;
    }

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.playback.ru/").get();
        String html = doc.html();
        String text = cleanHtml(html);

        Lemmatizer lemmatizer = new Lemmatizer();
        Map<String, Integer> lemmaCountMap = lemmatizer.lemmatizeText(text.toLowerCase());

        // Выводим результаты
        for (Map.Entry<String, Integer> entry : lemmaCountMap.entrySet()) {
            System.out.println(entry.getKey() + " — " + entry.getValue());
        }
    }
}


