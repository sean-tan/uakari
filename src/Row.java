class Row {
    private boolean downloading;
    private String url;

    public Row(boolean downloading, String url) {
        this.downloading = downloading;
        this.url = url;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public Object value(DownloadsColumnModel.Col col) {
        switch (col) {
            case DOWNLOAD:
                return downloading;
            case FILE:
                return url;
            default:
                throw new IllegalArgumentException("no data for column " + col);
        }
    }

    public String getUrl() {
        return url;
    }
}
