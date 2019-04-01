package com.tolbakova.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;

public class Crawler {
    private static Logger LOG = Logger.getLogger(Crawler.class.getName());

    private static final String TAG_NAME = "a";
    private static final String ATTRIBUTE_NAME = "href";
    private static final String HTML5_JOINER = "#";
    private static final String ROOT = "/";

    private final ConcurrentLinkedDeque<Entry> deque = new ConcurrentLinkedDeque<>();
    private final String domain;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Set<String> foundEntries = new ConcurrentSkipListSet<>();
    private Predicate<Entry> existsPredicate = entry -> !foundEntries.contains(entry.getUrl());

    public Crawler(String domain) {
        this.domain = domain;
    }

    public Entry getSiteMap() {
        LOG.info("Start crawling job for " + domain);
        Entry rootEntry = new Entry(ROOT);
        deque.add(rootEntry);
        List<CompletableFuture> jobs = new ArrayList<>();
        while (!deque.isEmpty() || !jobs.stream().allMatch(CompletableFuture::isDone)) {
            jobs.add(runJob());
        }
        return rootEntry;
    }

    public Set<String> getAllUrls() {
        if (foundEntries.isEmpty()) {
            getSiteMap();
        }
        return foundEntries;
    }

    private CompletableFuture<Void> runJob() {
        return runAsync(() -> {
            try {
                Entry processingEntry = deque.pollFirst();
                if (processingEntry == null) {
                    return;
                }
                if (foundEntries.contains(processingEntry.getUrl())) {
                    processingEntry.setEntries(emptyList());
                    return;
                }
                foundEntries.add(processingEntry.getUrl());
                List<Entry> crawled = getEntriesOnThePage(processingEntry);
                processingEntry.setEntries(crawled);
                crawled.stream().filter(existsPredicate).forEach(deque::add);
                LOG.info("Processed url: " + processingEntry.getUrl());
            } catch (IOException e) {
                LOG.warning("Url cannot be processed: " + e.getMessage());
            }
        }, executorService);
    }

    private List<Entry> getEntriesOnThePage(Entry processingEntry) throws IOException {
        Document page = Jsoup.connect(domain + processingEntry.getUrl()).get();
        return page.select(TAG_NAME).stream()
                .map(element -> element.attr(ATTRIBUTE_NAME).split(HTML5_JOINER)[0])
                .filter(ref -> ref.startsWith(ROOT) && !ref.equals(processingEntry.getUrl()))
                .distinct()
                .map(Entry::new)
                .collect(toList());
    }
}
