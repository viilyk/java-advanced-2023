package info.kgeorgiy.ja.ilyk.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    Downloader downloader;
    ExecutorService executorDownloaders;
    ExecutorService executorExtractors;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        executorDownloaders = Executors.newFixedThreadPool(downloaders);
        executorExtractors = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        return new Worker().download(url, depth);
    }

    @Override
    public void close() {
        executorDownloaders.shutdownNow();
        executorExtractors.shutdownNow();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Wrong arguments. Expected: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        try {
            int depth = (1 < args.length) ? Integer.parseInt(args[1]) : 1;
            int downloaders = (2 < args.length) ? Integer.parseInt(args[2]) : 1;
            int extractors = (3 < args.length) ? Integer.parseInt(args[3]) : 1;
            int perHost = (4 < args.length) ? Integer.parseInt(args[4]) : 1;
            try (Crawler crawler = new WebCrawler(new CachingDownloader(1), downloaders, extractors, perHost)) {
                crawler.download(args[0], depth);
            } catch (IOException e) {
                System.err.println("Downloader initialization error " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("Parsing integer error " + e.getMessage());
        }
    }

    private class Worker {
        private final ConcurrentLinkedQueue<String> tasks = new ConcurrentLinkedQueue<>();
        private final List<String> downloaded = Collections.synchronizedList(new ArrayList<>());
        private final Map<String, IOException> errors = new ConcurrentHashMap<>();
        private final Phaser phaser = new Phaser(1);

        public Result download(String url, int depth) {
            tasks.add(url);
            Set<String> visited = ConcurrentHashMap.newKeySet();
            int size = 1;
            for (int i = depth; i > 0; i--) {
                for (int j = 0; j < size; j++) {
                    String u = tasks.poll();
                    if (visited.add(u)) {
                        downloadLink(u, i);
                    }
                }
                phaser.arriveAndAwaitAdvance();
                size = tasks.size();
            }
            return new Result(downloaded, errors);
        }

        private void downloadLink(String url, int depth) {
            phaser.register();
            executorDownloaders.submit(() -> {
                try {
                    Document document = downloader.download(url);
                    downloaded.add(url);
                    if (depth > 1) {
                        getLinks(document, url);
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }

        private void getLinks(Document document, String url) {
            phaser.register();
            executorExtractors.submit(() -> {
                try {
                    tasks.addAll(document.extractLinks());
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }
    }

}
