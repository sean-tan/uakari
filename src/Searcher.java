public interface Searcher {
    void searchWeb(String query);

    void scanPage(String url);

    void addSearchListener(SearchListener listener);

    void interuptSearch();
}
