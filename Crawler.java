package com.crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Crawler {

    private ExecutorService executor;
    private AtomicInteger counter = new AtomicInteger();
    private List<SiteUrl> pages = new LinkedList<>();
    private LinkedBlockingQueue<Future<SiteUrl>> queue = new LinkedBlockingQueue();

    private ConcurrentMap<String, String> visitPageMap = new ConcurrentHashMap<>();
    private String domain;

    public Crawler(String domain) {
        executor = Executors.newFixedThreadPool(1);
        this.domain = domain;
    }

    public static void main(String[] args) throws Exception {
        try {
            if(args.length!=1){
                usage();
                System.exit(0);
            }

            String startPage = args[0];
            List<SiteUrl> pages = new Crawler(startPage).crawl(startPage);
            pages.forEach(x -> {
                System.out.println("=== page info ====\n\n\n\n" + x);
            });
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static void usage(){
        System.out.println("USAGE: java -jar crawler-1.0-SNAPSHOT.jar www.example.com ");
    }

    public List<SiteUrl> crawl(String link) throws InterruptedException, ExecutionException {
        visitUrlIfNotVisitedBefore(link);

        //wait so queue is ready
        Thread.sleep(1000);

        while (!queue.isEmpty()) {
            pages.add(queue.poll().get());
        }

        executor.shutdown();
        return pages;
    }

    private void visitUrlIfNotVisitedBefore(String visitUrl) {
        if (visitPageMap.putIfAbsent(visitUrl, "") == null) {
            try {
                Future<SiteUrl> submit = executor.submit(() -> {
                    counter.incrementAndGet();
                    try {
                        return visitLink(visitUrl);
                    } catch (IOException e) {
                        // Put some retry logic if resource is not available or response is other format.
                        return new SiteUrl(visitUrl, e.getMessage());
                    }
                });
                queue.put(submit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public SiteUrl visitLink(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Elements img = doc.getElementsByTag("img");
        Elements scripts = doc.getElementsByTag("script");
        Elements imports = doc.select("link[href]");
        Elements linksOnSameDomain = doc.select("a[href^=" + domain + "]");
        Elements externalLinks = doc.select("a[href~=^((?!" + domain + ").)*$]");
        SiteUrl page = new SiteUrl(link, linksOnSameDomain, externalLinks, img, scripts, imports);
        linksOnSameDomain.forEach(element -> visitUrlIfNotVisitedBefore(element.attr("abs:href")));
        return page;
    }

}
