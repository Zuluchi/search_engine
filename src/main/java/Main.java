import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {

        //UrlParserThreadPool parserThreadPool = new UrlParserThreadPool("https://dimonvideo.ru/");
        Lemmatizer lemmatizer = new Lemmatizer("Повторное появление леопарда в Осетии позволяет предположить, что " +
                "леопард постоянно обитает в некоторых районах Северного Кавказа.");
//        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
//        List<String> wordBaseForms = luceneMorphology.getNormalForms("некоторых");
//        wordBaseForms.forEach(System.out::println);
//        List<String> wordBaseFormsw = luceneMorphology.getMorphInfo("некоторых");
//        wordBaseFormsw.forEach(System.out::println);
    }
}
