package application.repositories;

import application.models.Page;
import application.models.Site;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PageRepository extends CrudRepository<Page, Integer> {

    long countBySiteBySiteId(Site siteBySiteId);

    Optional<Page> findByPathAndSiteBySiteId(String path, Site site);

    @Override
    @Modifying
    @Query("DELETE FROM Page")
    void deleteAll();
}
