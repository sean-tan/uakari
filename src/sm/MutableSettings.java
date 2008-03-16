package sm;

import java.io.File;

class MutableSettings implements Settings {
    private String downloadPath;
    private String password;
    private String username;

    public MutableSettings(String downloadPath, String username, String password) {
        this.downloadPath = downloadPath;
        this.password = password;
        this.username = username;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public File getDownloadPath() {
        return new File(downloadPath);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
