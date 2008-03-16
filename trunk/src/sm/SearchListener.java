package sm;

public interface SearchListener {
    void addSearchResult(String parentPage, String url);

    void searchFinished();
}
