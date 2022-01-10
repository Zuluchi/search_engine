package application.repositories;

import application.models.Index;
import application.models.dto.interfaces.IndexPageId;
import application.models.dto.interfaces.PageRelevance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {

    List<IndexPageId> findByLemmaId(int lemmaId);

    List<IndexPageId> findByLemmaIdAndPageIdIn(int lemmaId, List<Integer> pageIds);

    @Query(value = "SELECT page_id AS pageID, SUM(lemma_rank)/relrev.maxrev AS relevance FROM search_engine._index JOIN " +
            "(SELECT MAX(absrev) AS maxrev FROM (SELECT page_id, SUM(lemma_rank) AS absrev FROM search_engine._index " +
            "WHERE page_id IN (?1)" +
            "AND lemma_id IN (?2)" +
            "GROUP BY page_id) AS result) AS relrev " +
            "WHERE page_id IN (?1)" +
            "AND lemma_id IN (?2)" +
            "GROUP BY page_id order by page_id;", nativeQuery = true)
    List<PageRelevance> findPageRelevance(List<Integer> pageIds, List<Integer> lemmaIds);


}
