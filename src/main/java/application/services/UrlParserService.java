package application.services;

import application.Lemmatizer;
import application.Main;
import application.UrlParser;
import application.config.SitesConfig;
import application.models.Site;
import application.models.SiteStatusType;
import application.repositories.SiteRepository;
import application.responses.ResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
public class UrlParserService {
    @Autowired
    private SitesConfig sitesConfig;
    @Autowired
    private IndexService indexService;
    @Autowired
    private LemmaService lemmaService;
    @Autowired
    private PageService pageService;
    @Autowired
    private SiteService siteService;

    public ResultResponse startIndexing(){
        ArrayList<Site> sites = sitesConfig.getSites();
        for (Site site : sites) {
            CompletableFuture.runAsync(() ->  {
                try {
                    asyncStartIndexing(site);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, ForkJoinPool.commonPool());
        }
        return new ResultResponse();
    }

    @Async
    void asyncStartIndexing(Site site) throws IOException {
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        site.setStatus(SiteStatusType.INDEXING);
        siteService.saveSite(site);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Lemmatizer lemmatizer = new Lemmatizer();
        Set<String> parsedURLs = Collections.synchronizedSet(new HashSet<>());
        parsedURLs.add(site.getUrl().toLowerCase(Locale.ROOT));
        UrlParser urlParser = new UrlParser(site.getUrl().toLowerCase(Locale.ROOT), site, parsedURLs);
        urlParser.setIndexService(indexService);
        urlParser.setLemmaService(lemmaService);
        urlParser.setPageService(pageService);
        urlParser.setSiteService(siteService);
        urlParser.setLemmatizer(lemmatizer);
        forkJoinPool.invoke(urlParser);

        siteService.updateStatus(site);}
}
