package application.services;

import application.models.Index;
import application.models.Lemma;
import application.models.Page;
import application.repositories.IndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class IndexService {
    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private FieldService fieldService;

    public void addIndex(Index index){
        indexRepository.save(index);
    }

    public void createIndexAndSave(Page page, Map<String, Float> lemmasAndRank,
                            Map<String, Lemma> lemmas,
                            Map<String, Float> titleLemms,
                            Map<String, Float> bodyLemms){
        createIndex(page, lemmasAndRank, lemmas, titleLemms, 1);
        createIndex(page, lemmasAndRank, lemmas, bodyLemms, 2);
    }

    private void createIndex(Page page,
                             Map<String, Float> lemmasAndRank,
                             Map<String, Lemma> lemmas,
                             Map<String, Float> lemmsOnField, int fieldId) {
        for (Map.Entry<String, Float> lemma : lemmsOnField.entrySet()) {
            Index index = new Index();
            index.setPageByPageId(page);
            index.setFieldByFieldId(fieldService.getById(fieldId)
                    .orElseThrow(() -> new NullPointerException("field " + fieldId + " Not Found")));
            index.setLemmaByLemmaId(lemmas.get(lemma.getKey()));
            index.setLemmaRank(lemmasAndRank.get(lemma.getKey()));

            addIndex(index);
        }
    }
}
