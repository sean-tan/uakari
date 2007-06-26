import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlHeader2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Google {
    public static List<String> searchFor(String text) throws IOException {
        String query = URLEncoder.encode(text, "UTF-8");
        WebClient client = new WebClient();
        HtmlPage webResponse = (HtmlPage) client.getPage("http://www.google.co.uk/search?num=20&hl=en&safe=off&q=" + query + "&btnG=Search&meta=");
        List<HtmlHeader2> h2List = webResponse.getDocumentElement().getHtmlElementsByTagName("h2");
        List<String> results = new ArrayList();

        for (HtmlHeader2 htmlHeader2 : h2List) {
            HtmlAnchor link = (HtmlAnchor) htmlHeader2.getHtmlElementsByTagName("a").get(0);
            results.add(link.getHrefAttribute());
        }

        return results;
    }
}
