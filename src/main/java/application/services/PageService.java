package application.services;

import application.models.Page;
import application.models.Site;
import application.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {
    @Autowired
    private PageRepository pageRepository;

    public int addSiteAndReturnId(Page page){
        pageRepository.save(page);
        return page.getId();
    }

    public Page createPageAndSave(String path, int code, String content, Site site){
        Page page = new Page();
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        page.setSiteBySiteId(site);
        pageRepository.save(page);
        return page;
    }
}
