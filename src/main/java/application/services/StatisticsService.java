package application.services;

import application.models.dto.statistics.DetailedStatisticsDto;
import application.models.dto.statistics.StatisticsDto;
import application.models.dto.statistics.TotalStatisticsDto;
import application.repositories.LemmaRepository;
import application.repositories.PageRepository;
import application.repositories.SiteRepository;
import application.responses.StatisticResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;


    public StatisticResponse getStatistics() {
        TotalStatisticsDto totalStatisticsDto = new TotalStatisticsDto(siteRepository.count(), pageRepository.count(),
                lemmaRepository.count(), true);

        List<DetailedStatisticsDto> detailedStatisticsDtoList = new ArrayList<>();
        siteRepository.findAll().forEach(site -> {
            DetailedStatisticsDto detailedStatisticsDto = new DetailedStatisticsDto(site.getUrl(), site.getName(),
                    site.getStatus(), site.getStatusTime(), site.getLastError(),
                    pageRepository.countBySiteBySiteId(site), lemmaRepository.countBySiteBySiteId(site));
            detailedStatisticsDtoList.add(detailedStatisticsDto);
        });

        return new StatisticResponse(new StatisticsDto(totalStatisticsDto, detailedStatisticsDtoList));
    }

}
