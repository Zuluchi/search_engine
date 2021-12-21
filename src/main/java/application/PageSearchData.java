package application;

public class PageSearchData implements Comparable<PageSearchData> {
    private int pageId;
    private String path;
    private String html;
    private float relevance;
    private String snippet;
    private String title;

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public float getRelevance() {
        return relevance;
    }

    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String findQuery) {
        this.snippet = JsoupData.getSearchedTextInHtml(this.html, findQuery);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PageSearchData(int pageId, String path, String html, float relevance) {
        this.pageId = pageId;
        this.path = path;
        this.html = html;
        this.relevance = relevance;
        this.title = JsoupData.getTitle(html);
    }

    @Override
    public int compareTo(PageSearchData o) {
        return Float.compare(o.relevance, this.relevance);
    }
}
