package application.responses;

import application.models.dto.statistics.StatisticsDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StatisticResponse {
    private String result = "true";

    private StatisticsDto statistics;

    public StatisticResponse(StatisticsDto statisticsDto) {
        this.statistics = statisticsDto;
    }
}
