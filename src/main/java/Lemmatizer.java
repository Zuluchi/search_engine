import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemmatizer {
    private final String wordTypeRegex = ".*(СОЮЗ|МЕЖД|ПРЕДЛ|ЧАСТ)$";
    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();

    public Lemmatizer() throws IOException {
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = textToArray(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (isCorrectWordType(word) && !word.isEmpty()) {
                List<String> wordBaseForms = luceneMorphology.getNormalForms(word);
                lemmaSet.addAll(wordBaseForms);
            }
        }
        return lemmaSet;
    }

    public Map<String, Float> countLemmasOnField(String text) {
        Map<String, Float> wordCountMap = new HashMap<>();
        String[] textArray = textToArray(text);
        for (String word : textArray) {
            if (isCorrectWordType(word) && !word.isEmpty()) {
                List<String> wordBaseForms = luceneMorphology.getNormalForms(word);
                for (String lemma : wordBaseForms) {
                    wordCountMap.put(lemma, wordCountMap.getOrDefault(lemma, 0f) + 1);
                }
            }
        }
        return wordCountMap;
    }

    public HashMap<String, Float> calculateLemmasRank(Map<String, Integer> lemmasId,
                                                      Map<String, Float> titleFieldLemmas,
                                                      Map<String, Float> bodyFieldLemmas) {
        HashMap<String, Float> lemmasAndRankMap = new HashMap<>();
        for (Map.Entry<String, Integer> lemma : lemmasId.entrySet()) {
            float rank = titleFieldLemmas.getOrDefault(lemma.getKey(), 0f) * 1.0f
                    + bodyFieldLemmas.getOrDefault(lemma.getKey(), 0f) * 0.8f;
            lemmasAndRankMap.put(lemma.getKey(), rank);
        }

        return lemmasAndRankMap;
    }

    private String[] textToArray(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("([^а-я\\s])", " ")
                .replaceAll("\\s+", " ").split(" ");
    }

    private boolean isCorrectWordType(String word) {
        try {
            List<String> wordInfo = luceneMorphology.getMorphInfo(word);
            for (String morphInfo : wordInfo) {
                if (morphInfo.matches(wordTypeRegex)) {
                    return false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(word);
        }
        return true;
    }
}
