package sm;

class Row {
    private boolean downloading;
    private String url;
    private final String parentPage;

    public Row(boolean downloading, String url, String parentPage) {
        this.downloading = downloading;
        this.url = url;
        this.parentPage = parentPage;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public Object value(DownloadsColumnModel.Col col) {
        switch (col) {
            case DOWNLOAD:
                return downloading;
            case RAPIDSHARE_FILE:
                return url;
            case PARENT_PAGE:
                return parentPage;
            default:
                throw new IllegalArgumentException("no data for column " + col);
        }
    }

    public String getUrl() {
        return url;
    }
}
