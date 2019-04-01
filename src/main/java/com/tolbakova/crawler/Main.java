package com.tolbakova.crawler;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Specify domain to be crawled");
        } else {
            String domain = args[0];
            System.out.println(new Crawler(domain).getAllUrls());
        }
    }
}
