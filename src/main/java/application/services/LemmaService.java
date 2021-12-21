package application.services;

import application.models.Lemma;
import application.models.Site;
import application.repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Service
public class LemmaService {
    @Autowired
    private LemmaRepository lemmaRepository;

    @Transactional()
    public HashMap<String, Lemma> createAndInsertLemmaOnDuplicateUpdateAndGetMap(Site site, Set<String> lemmaSet) {
        HashMap<String, Lemma> lemmaMap = new HashMap<>();
        for (String lemmaString : lemmaSet) {
            Optional<Lemma> optionalLemma = lemmaRepository.findLemmaByLemmaAndSiteBySiteId(lemmaString, site);
            if (optionalLemma.isPresent()) {
                Lemma lemma = optionalLemma.get();
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);
            } else {
                Lemma lemma = new Lemma(lemmaString, 1, site);
                lemmaRepository.save(lemma);
                lemmaMap.put(lemma.getLemma(), lemma);
            }
        }
        return lemmaMap;
    }

//    public HashMap<String,Lemma> createAndInsertLemmaOnDuplicateUpdateAndGetMap(Site site, Set<String> lemmaSet) {
//        HashMap<String,Lemma> lemmaMap = new HashMap<>();
//        for (String lemmaString : lemmaSet) {
//            lemmaRepository.insertOnDuplicateUpdate(1,lemmaString,site.getId());
//        }
//        List<Lemma> lemmaList = lemmaRepository.getAllByLemmaInAndSiteBySiteId(lemmaSet,site);
//
//        for (Lemma lemma : lemmaList){
//            lemmaMap.put(lemma.getLemma(),lemma);
//        }
//        return lemmaMap;
//    }
}
