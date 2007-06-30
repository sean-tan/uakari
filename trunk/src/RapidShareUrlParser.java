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

    public RapidShareUrlParser(Audit audit) {
        this.audit = audit;
    }

    public void foreachUrlIn(List<String> pages, UrlHandler urlHandler) throws IOException, InterruptedException {
        for (String page : pages) {

            TimeUnit.SECONDS.sleep(1);

            audit.addMessage("Scanning " + page + " for rapidshare files");
            try {
                WebClient client = new WebClient(BrowserVersion.getDefault());
                client.setJavaScriptEnabled(false);
                client.setPrintContentOnFailingStatusCode(false);
                client.setThrowExceptionOnFailingStatusCode(true);
                client.setThrowExceptionOnScriptError(false);
                client.setRefreshHandler(new WaitingRefreshHandler());

                Page webResponse = client.getPage(page);
                String s = webResponse.getWebResponse().getContentAsString();
                if (s == null)//not sure why this happens
                    continue;

                Pattern pattern = Pattern.compile("(rapidshare.com(/|%2F)[^\\s|>|<|\"|\\(|\\)|\\\\]+)", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(s);
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
