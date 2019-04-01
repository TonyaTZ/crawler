package com.tolbakova.crawler;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

public class CrawlerTest {


    @Test
    @Ignore
    public void canParsePage() {
        Entry crawl = new Crawler("http://monzo.com").getSiteMap();
        Set<String> allUrls = new Crawler("http://monzo.com").getAllUrls();
        System.out.println(crawl);
        System.out.println(String.join(", ", allUrls));
    }
}
