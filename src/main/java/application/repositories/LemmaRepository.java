package application.repositories;

import application.models.Lemma;
import application.models.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma,Integer> {

    @Modifying(clearAutomatically=true, flushAutomatically = true)
    @Query(value="INSERT INTO search_engine._lemma (frequency, lemma, site_id) " +
            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE frequency = frequency + 1;", nativeQuery=true)
    void insertOnDuplicateUpdate(int frequency, String lemma, int site_id);


    List<Lemma> getAllByLemmaInAndSiteBySiteId(Collection<String> lemmas, Site siteBySiteId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Lemma> findLemmaByLemmaAndSiteBySiteId(String lemma, Site site);

    @Override
    <S extends Lemma> S save(S entity);


}
