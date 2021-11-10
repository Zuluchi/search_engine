import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Lemmatizer {
    private final String wordTypeRegex = ".*(СОЮЗ|МЕЖД|ПРЕДЛ|ЧАСТ)$";
    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();

    public Lemmatizer(String text) throws IOException {
        Map<String, Integer> wordCountMap = new HashMap<>();
        String[] textArray = text.toLowerCase(Locale.ROOT).replaceAll("[^а-я\\s]", "").split(" ");
        for (String word : textArray) {
            if(isCorrectWordType(word)) {
                List<String> wordBaseForms = luceneMorphology.getNormalForms(word);
                for (String lemma : wordBaseForms) {
                    wordCountMap.put(lemma, wordCountMap.getOrDefault(lemma, 0) + 1);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    private boolean isCorrectWordType(String word) {
        AtomicBoolean isCorrect = new AtomicBoolean(true);
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        wordInfo.forEach(morphInfo -> {
            if (morphInfo.matches(wordTypeRegex)) {
                isCorrect.set(false);
            }
        });
        return isCorrect.get();
    }
}
