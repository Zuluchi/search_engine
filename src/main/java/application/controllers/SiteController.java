package application.controllers;

import application.responses.ResultResponse;
import application.services.SiteService;
import application.services.UrlParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
public class SiteController {
    @Autowired
    private UrlParserService urlParserService;

    @GetMapping("/api/startIndexing")
    public ResponseEntity<ResultResponse> startIndexing() throws SQLException, IOException {

        return ResponseEntity.ok(urlParserService.startIndexing());
    }
}
