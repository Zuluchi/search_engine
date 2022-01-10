package application.models.dto.interfaces;

import application.models.Site;

public interface PageSearchModel {
    int getId();

    Site getSiteBySiteId();

    String getPath();

    String getContent();
}
