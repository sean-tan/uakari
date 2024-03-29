package sm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ThreadedSearcher implements Searcher {
    private final ExecutorService executorService;
    private final RapidShareUrlParser rapidShareUrlParser;
    private final Audit audit;
    private List<SearchListener> listeners = new ArrayList<SearchListener>();
    private Future<?> work;

    public ThreadedSearcher( ExecutorService executorService, RapidShareUrlParser rapidShareUrlParser, Audit audit) {
        this.executorService = executorService;
        this.rapidShareUrlParser = rapidShareUrlParser;
        this.audit = audit;
    }

    public void interuptSearch() {
        if (this.work != null)
            this.work.cancel(true);
        finsihed();
    }

    public void searchWeb(final String query) {
        this.work = executorService.submit(new Runnable() {
            public void run() {
                try {
                    List<String> pages = Google.searchFor("rapidshare.com " + query);
                    rapidShareUrlParser.foreachUrlIn(pages, new UrlHandler() {
                        public void handle(String parentPage, String url) {
                            addUrl(parentPage, url);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    audit.addMessage(e);
                } finally {
                    finsihed();
                }
            }
        });
    }

    public void scanPage(final String url) {
        this.work = executorService.submit(new Runnable() {
            public void run() {
                try {
                    rapidShareUrlParser.foreachUrlIn(Collections.singletonList(url), new UrlHandler() {
                        public void handle(String parentPage, String url) {
                            addUrl(parentPage, url);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    audit.addMessage(e);
                } finally {
                    finsihed();
                }
            }
        });
    }

    private void finsihed() {
        for (SearchListener listener : listeners) {
            listener.searchFinished();
        }
    }

    public void addSearchListener(SearchListener listener) {
        this.listeners.add(listener);
    }

    private void addUrl(String parentPage, String url) {
        for (SearchListener listener : listeners) {
            listener.addSearchResult(parentPage, url);
        }
    }
}
