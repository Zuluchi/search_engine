package application.services.UrlParser;

import application.models.Lemma;
import application.models.Page;
import application.models.Site;
import application.services.IndexService;
import application.services.LemmaService;
import application.services.PageService;
import application.services.SiteService;
import application.utils.JsoupData;
import application.utils.Lemmatizer;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.RecursiveAction;


@Setter
public class UrlParser extends RecursiveAction {
    private final static String urlRegex = "(?i).*(\\.(doc|pdf|xml|xls|xlsx|jpg|jpeg|gif|png|rar|zip|exe|bin|ppt|apk|"
            + "jar|mp3|aac|csv|json|eps|nc|fig)|/{3,}|#).*$";
    private final Set<String> urlSet;
    private final String url;
    private final String rootUrl;
    private final Site site;
    private Lemmatizer lemmatizer;
    private IndexService indexService;
    private LemmaService lemmaService;
    private PageService pageService;
    private SiteService siteService;

    public UrlParser(String url, Site site, Set<String> urlSet) {
        this.url = url.toLowerCase(Locale.ROOT);
        this.urlSet = urlSet;
        this.rootUrl = site.getUrl();
        this.site = site;
    }

    @Override
    protected void compute() {
        if (!siteService.isIndexingStopFlag()) {
            List<UrlParser> tasks = new LinkedList<>();
            try {
                Connection connection = JsoupData.createConnection(url);
                Thread.sleep(400);
                insertData(connection);

                Elements tagA = JsoupData.getElementsByTagA(connection);
                for (Element element : tagA) {
                    String lowerCaseElementUrl = element.absUrl("href").toLowerCase(Locale.ROOT);
                    if (isUrlCorrect(lowerCaseElementUrl)) {
                        urlSet.add(lowerCaseElementUrl);
                        UrlParser subTask = new UrlParser(lowerCaseElementUrl, site, urlSet);
                        subTask.setIndexService(indexService);
                        subTask.setLemmaService(lemmaService);
                        subTask.setPageService(pageService);
                        subTask.setSiteService(siteService);
                        subTask.setLemmatizer(lemmatizer);
                        subTask.fork();
                        tasks.add(subTask);
                    }
                }
            } catch (IOException | InterruptedException | SQLException e) {
                siteService.updateErrorMessage(site, url + " - " + e.getMessage());
            }
            for (UrlParser parser : tasks) {
                parser.join();
            }
        }
    }

    public void insertData(Connection connection) throws IOException, SQLException, InterruptedException {
        int responseCode = JsoupData.getResponseCode(connection);
        siteService.updateStatusTime(site);
        Page page = pageService.createPageAndSave(url.substring(rootUrl.length()), responseCode,
                connection.get().html(), site);
        String bodyText = JsoupData.getBodyText(connection);
        String titleText = JsoupData.getTitleText(connection);

        if (responseCode == 200) {
            Map<String, Lemma> lemmaMap = lemmaService.createAndInsertLemmaOnDuplicateUpdateAndGetMap(site,
                    lemmatizer.getLemmaSet(bodyText + " " + titleText));
            Map<String, Float> titleLemmasCount = lemmatizer.countLemmasOnField(titleText);
            Map<String, Float> bodyLemmasCount = lemmatizer.countLemmasOnField(bodyText);
            Map<String, Float> lemmasAndRank = lemmatizer.calculateLemmasRank(lemmaMap, titleLemmasCount, bodyLemmasCount);
            indexService.createIndexAndSave(page, lemmasAndRank, lemmaMap, titleLemmasCount, bodyLemmasCount);
        }
    }

    private boolean isUrlCorrect(String url) {
        return url.startsWith(rootUrl) && !url.matches(urlRegex) && !urlSet.contains(url);
    }
}