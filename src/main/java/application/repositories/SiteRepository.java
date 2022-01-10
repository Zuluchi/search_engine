package application.repositories;

import application.models.Site;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SiteRepository extends CrudRepository<Site, Integer> {

    Optional<Site> findByName(String siteName);

    Optional<Site> findByUrl(String url);
}
