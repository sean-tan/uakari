class TableModelSearchListener implements SearchListener {
    private final DownloadsTableModel tableModel;
    private final SwingAudit audit;
    private final RapidForm form;

    public TableModelSearchListener(DownloadsTableModel tableModel, SwingAudit audit, RapidForm form) {
        this.tableModel = tableModel;
        this.audit = audit;
        this.form = form;
    }

    public void addSearchResult(String url) {
        if (!tableModel.containsUrl(url)) {
            try {
                RapidShareResourceFinder.checkUrlValidity(url);
                tableModel.add(new Row(false, url));
            } catch (InvalidRapidshareUrlException e) {
                audit.addMessage(e);
            }
        }
    }

    public void searchFinished() {
        form.searchFinished();
    }
}
