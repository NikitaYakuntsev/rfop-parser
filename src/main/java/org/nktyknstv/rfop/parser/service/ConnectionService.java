package org.nktyknstv.rfop.parser.service;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Date;

@UtilityClass
@Slf4j
public class ConnectionService {

    public Document getPage(String plainUrl) throws InterruptedException {
        String sign = plainUrl.contains("?")
                ? "&"
                : "?";
        String url = String.format("%s%s%s=%d", plainUrl, sign, RandomStringUtils.randomAlphabetic(3), new Date().getTime());
        log.info("Trying to retrieve page {}", url);
        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
                .ignoreHttpErrors(true)
                .referrer("https://www.google.com")
                .maxBodySize(100 * 1024 * 1024)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
                .header("Connection", "keep-alive")
                .header("Host", "profitcon.ru")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Upgrade-Insecure-Requests", "1");
        Document page = null;

        int tries = 1;
        do {
            try {
                page = connection.get();
            } catch (Exception e) {
                log.warn("Trying to access url {} for {} time. Error while doing request: {}", url, tries, e.getMessage());
                Thread.sleep(500);
                tries++;
            }
        }  while (page == null && tries <= 10);
        if (page != null) {
            log.info("Page {} retrieved successfully", url);
        } else {
            log.warn("Unable to get page {}", url);
        }
        return page;
    }
}
