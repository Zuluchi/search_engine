package application.services;

import application.models.Page;
import application.models.Site;
import application.models.dto.interfaces.PageRelevance;
import application.models.dto.interfaces.PageSearchModel;
import application.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    private PageRepository pageRepository;

    public Page createPageAndSave(String path, int code, String content, Site site) {
        Page page = new Page();
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        page.setSiteBySiteId(site);
        pageRepository.save(page);
        return page;
    }

    @Transactional
    public Optional<Page> getPageByPath(String path) {
        return pageRepository.findByPath(path);
    }

    public void deletePage(Page page) {
        pageRepository.delete(page);
    }

    public List<PageSearchModel> findPageData(List<PageRelevance> pageRelevanceList) {
        List<Integer> pageIds = new ArrayList<>();
        pageRelevanceList.forEach(pageRelevance -> pageIds.add(pageRelevance.getPageId()));
        return pageRepository.findByIdInOrderById(pageIds);
    }
}
