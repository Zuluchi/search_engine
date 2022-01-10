package application.services;

import application.models.Index;
import application.models.Lemma;
import application.models.Page;
import application.models.dto.interfaces.IndexPageId;
import application.models.dto.interfaces.ModelId;
import application.models.dto.interfaces.PageRelevance;
import application.repositories.IndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IndexService {
    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private FieldService fieldService;

    public void addIndex(Index index) {
        indexRepository.save(index);
    }

    public void createIndexAndSave(Page page, Map<String, Float> lemmasAndRank,
                                   Map<String, Lemma> lemmas,
                                   Map<String, Float> titleLemms,
                                   Map<String, Float> bodyLemms) {
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

    @Transactional(readOnly = true)
    public List<IndexPageId> findPagesIds(int lemmaId) {
        return indexRepository.findByLemmaId(lemmaId);
    }

    @Transactional(readOnly = true)
    public List<IndexPageId> getPagesIdOfNextLemmas(int lemmaId, List<IndexPageId> pageIdList) {
        ArrayList<Integer> pageIds = new ArrayList<>();
        pageIdList.forEach(indexPageId -> pageIds.add(indexPageId.getPageId()));
        return indexRepository.findByLemmaIdAndPageIdIn(lemmaId, pageIds);
    }

    @Transactional(readOnly = true)
    public List<PageRelevance> findPageRelevance(List<IndexPageId> pageIdList, List<ModelId> lemmaIdList) {
        ArrayList<Integer> pageIds = new ArrayList<>();
        pageIdList.forEach(indexPageId -> pageIds.add(indexPageId.getPageId()));
        ArrayList<Integer> lemmaIds = new ArrayList<>();
        lemmaIdList.forEach(indexLemmaId -> lemmaIds.add(indexLemmaId.getId()));
        return indexRepository.findPageRelevance(pageIds, lemmaIds);
    }
}
