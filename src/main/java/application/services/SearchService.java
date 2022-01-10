package application.services;

import application.models.dto.interfaces.IndexPageId;
import application.models.dto.interfaces.ModelId;
import application.models.dto.interfaces.PageRelevance;
import application.models.dto.interfaces.PageSearchModel;
import application.models.dto.search.PageSearchDto;
import application.responses.ErrorResponse;
import application.responses.SearchResponse;
import application.utils.JsoupData;
import application.utils.Lemmatizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class SearchService {
    private final Lemmatizer lemmatizer;
    @Autowired
    LemmaService lemmaService;
    @Autowired
    IndexService indexService;
    @Autowired
    PageService pageService;
    @Autowired
    SiteService siteService;

    public SearchService() throws IOException {
        this.lemmatizer = new Lemmatizer();
    }

    public Object search(String findQuery, String site, int offset, int limit) throws SQLException {
        Set<String> findQueryLemms = lemmatizer.getLemmaSet(findQuery);

        List<ModelId> lemmasIdList = (site == null) ? lemmaService.findLemmas(findQueryLemms) :
                lemmaService.findLemmasBySite(findQueryLemms, siteService.findSiteByName(site));

        if (!lemmasIdList.isEmpty()) {
            List<IndexPageId> searchedPagesId = indexService.findPagesIds(lemmasIdList.get(0).getId());
                for (int lemma = 1; lemma < lemmasIdList.size() - 1; lemma++) {
                    searchedPagesId = indexService.getPagesIdOfNextLemmas(lemmasIdList.get(lemma).getId(), searchedPagesId);
                }

            int pageLimit = Math.min(searchedPagesId.size(), limit);
            int pageOffset = Math.min(searchedPagesId.size(), offset);

            List<PageRelevance> pageIdAndRelevance = indexService.findPageRelevance(
                    searchedPagesId.subList(pageOffset, pageLimit), lemmasIdList);
            List<PageSearchModel> pageData = pageService.findPageData(pageIdAndRelevance);

            ArrayList<PageSearchDto> searchResult = new ArrayList<>();
            for (int page = 0; page <= pageIdAndRelevance.size() - 1; page++) {
                PageSearchDto searchDto = new PageSearchDto();
                searchDto.setSite(pageData.get(page).getSiteBySiteId().getUrl());
                searchDto.setSiteName(pageData.get(page).getSiteBySiteId().getName());
                searchDto.setUri(pageData.get(page).getPath());
                searchDto.setTitle(JsoupData.getTitle(pageData.get(page).getContent()));
                searchDto.setSnippet(JsoupData.getSnippetInHtml(pageData.get(page).getContent(), findQuery));
                searchDto.setRelevance(pageIdAndRelevance.get(page).getRelevance());

                searchResult.add(searchDto);
            }
            Collections.sort(searchResult);

            return new SearchResponse(searchResult.size(), searchResult);

        }
        return new ErrorResponse("Ничего не найдено");
    }
}
