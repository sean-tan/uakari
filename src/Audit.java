interface Audit {
    void addMessage(String message);

    void addMessage(Exception e);
}
