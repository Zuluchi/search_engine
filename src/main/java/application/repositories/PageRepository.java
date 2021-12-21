package application.repositories;

import application.models.Page;
import org.springframework.data.repository.CrudRepository;

public interface PageRepository extends CrudRepository<Page, Integer> {
}
