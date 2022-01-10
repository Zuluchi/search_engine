package application.repositories;

import application.models.Page;
import application.models.Site;
import application.models.dto.interfaces.PageSearchModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PageRepository extends CrudRepository<Page, Integer> {

    long countBySiteBySiteId(Site siteBySiteId);

    Optional<Page> findByPath(String path);

    List<PageSearchModel> findByIdInOrderById(Collection<Integer> id);

}
