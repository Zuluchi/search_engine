package application.repositories;

import application.models.Lemma;
import application.models.Site;
import application.models.dto.interfaces.ModelId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO search_engine._lemma (frequency, lemma, site_id) " +
            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE frequency = frequency + 1;", nativeQuery = true)
    void insertOnDuplicateUpdate(int frequency, String lemma, int site_id);

    Optional<Lemma> findLemmaByLemmaAndSiteBySiteId(String lemmaString, Site siteBySiteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE search_engine._lemma set frequency = frequency - 1 " +
            "where id in (select lemma_id from search_engine._index where page_id = ?);", nativeQuery = true)
    void unCountLemmasOfPage(int pageId);

    long countBySiteBySiteId(Site siteBySiteId);

    List<ModelId> findByLemmaInOrderByFrequency(Collection<String> lemmas);

    List<ModelId> findByLemmaInAndSiteBySiteIdOrderByFrequency(Collection<String> lemmas, Site site);
}
