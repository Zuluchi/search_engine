package application.services;

import application.Lemmatizer;
import application.config.SitesConfig;
import application.models.Site;
import application.models.SiteStatusType;
import application.repositories.SiteRepository;
import application.responses.ResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
public class SiteService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private SitesConfig sitesConfig;
    @Autowired
    private IndexService indexService;
    @Autowired
    private LemmaService lemmaService;
    @Autowired
    private PageService pageService;



    public void saveSite(Site site) {
        siteRepository.save(site);
    }

    public void updateStatusTime(Site site){
        site.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteRepository.save(site);
    }

    public void updateStatus(Site site){
        site.setStatus(SiteStatusType.INDEXED);
        siteRepository.save(site);
    }


}
