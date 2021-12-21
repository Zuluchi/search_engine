package application.repositories;

import application.models.Site;
import org.springframework.data.repository.CrudRepository;

public interface SiteRepository extends CrudRepository<Site, Integer> {
}
