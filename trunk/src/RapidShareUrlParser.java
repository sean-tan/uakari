import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WaitingRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RapidShareUrlParser {
    private final Audit audit;
    private static final Pattern URL_GUESS_PATTERN = Pattern.compile("(rapidshare.com(/|%2F)[^\\s|>|<|\"|\\(|\\)|\\\\]+)", Pattern.MULTILINE);

    public RapidShareUrlParser(Audit audit) {
        this.audit = audit;
    }

    public void foreachUrlIn(List<String> pages, UrlHandler urlHandler) throws IOException, InterruptedException {
        for (String page : pages) {

            TimeUnit.MICROSECONDS.sleep(1);

            audit.addMessage("Scanning " + page + " for rapidshare files");
            try {
                WebClient client = new WebClient(BrowserVersion.getDefault());
                client.setJavaScriptEnabled(false);
                client.setPrintContentOnFailingStatusCode(false);
                client.setThrowExceptionOnFailingStatusCode(true);
                client.setThrowExceptionOnScriptError(false);
                client.setRefreshHandler(new WaitingRefreshHandler());

                Page webResponse = client.getPage(page);
                String pageSource = new String(webResponse.getWebResponse().getResponseBody());
                Matcher matcher = URL_GUESS_PATTERN.matcher(pageSource);
                while (matcher.find()) {
                    String urlEnd = matcher.group(1).replaceAll("%2F", "/");
                    urlHandler.handle(page, "http://" + urlEnd);
                }
            } catch (Exception e) {
                audit.addMessage("broken page: " + page);
                audit.addMessage(e);
            }
        }
    }
}
