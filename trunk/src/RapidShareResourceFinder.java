import com.meterware.httpunit.Button;
import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class RapidShareResourceFinder {
    private final Random random = new Random(System.currentTimeMillis());
    private final Settings settings;
    private final Audit audit;

    public RapidShareResourceFinder(Settings settings, Audit audit) {
        this.settings = settings;
        this.audit = audit;
    }

    private boolean hasParameter(String name, WebForm form) {
        String[] strings = form.getParameterNames();
        for (String string : strings) {
            if (string.equals(name))
                return true;
        }
        return false;
    }

    public static String checkUrlValidity(String url) throws InvalidRapidshareUrlException {
        try {
            WebClient client = new WebConversation();
            ClientProperties properties = client.getClientProperties();
            properties.setAcceptCookies(true);
            properties.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8 ) Gecko/20051111 Firefox/1.5");
            properties.setAutoRedirect(true);

            WebResponse webResponse = client.getResponse(new GetMethodWebRequest(url));

            String fileNotFound = "File not found";
            String html = webResponse.getText();
            if (html.contains(fileNotFound))
                throw new InvalidRapidshareUrlException(url, fileNotFound);

            String deltedFile = "This file has been deleted";
            if (html.contains(deltedFile)) {
                throw new InvalidRapidshareUrlException(url, deltedFile);
            }
            String fileMarker = "You want to download the file <b>";
            int startIndex = html.indexOf(fileMarker);
            if (startIndex == -1)//not all rapdishare pages have this (such as a members homepage) - but most do
                return url;

            String end = html.substring(startIndex + fileMarker.length(), html.length());
            int endIndex = end.indexOf("</b>");
            return end.substring(0, endIndex);
        } catch (HttpNotFoundException e) {
            throw new InvalidRapidshareUrlException(url, "404 - file not found");
        } catch (InvalidRapidshareUrlException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRapidshareUrlException(url, e);
        }
    }

    public void connect(String url, ResourceHandler resourceHandler) throws IOException, SAXException, InvalidRapidshareUrlException, InterruptedException {
        WebClient client = new WebConversation();
        ClientProperties properties = client.getClientProperties();
        properties.setAcceptCookies(true);
        properties.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8 ) Gecko/20051111 Firefox/1.5");
        properties.setAutoRedirect(true);

        WebResponse webResponse = client.getResponse(new GetMethodWebRequest(url));

        WebForm[] forms = webResponse.getForms();
        for (WebForm form : forms) {
            String action = form.getAction();
            if (action.contains("rapidshare")) {
                Button[] buttons = form.getButtons();
                for (Button button : buttons) {
                    if (button.getValue().equals("PREMIUM")) {
                        WebResponse loginPage = form.submit((SubmitButton) button);
                        WebForm[] loginForms = loginPage.getForms();
                        for (WebForm loginForm : loginForms) {
                            if (loginForm.getAction().equals("/cgi-bin/premium.cgi") && hasParameter("accountid", loginForm)) {

                                loginForm.setParameter("accountid", settings.getUsername());
                                loginForm.setParameter("password", settings.getPassword());

                                WebResponse loginResponsePage = loginForm.submit();
                                WebForm[] downloadForms = loginResponsePage.getForms();
                                for (WebForm downloadForm : downloadForms) {
                                    if (hasParameter("dl.start", downloadForm)) {
                                        final WebResponse downloadPage = downloadForm.submit();
                                        List<WebLink> downloadLocations = new ArrayList() {{
                                            WebLink[] webLinks = downloadPage.getLinks();
                                            for (WebLink webLink : webLinks) {
                                                if (webLink.getText().startsWith("Download via")) {
                                                    add(webLink);
                                                }
                                            }
                                        }};

                                        WebLink webLink = downloadLocations.get(Math.abs(random.nextInt() % downloadLocations.size()));
                                        audit.addMessage(webLink.getText());
                                        String urlString = webLink.getURLString();
                                        GetMethodWebRequest request = new GetMethodWebRequest(urlString);
//                                                String contentRange = "bytes=-500";
//                                                request.setHeaderField("Range", contentRange);
                                        WebResponse theData = client.getResource(request);
                                        int total = theData.getContentLength();
                                        InputStream inputStream = theData.getInputStream();
                                        resourceHandler.handleStream(total, inputStream, url);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        throw new InvalidRapidshareUrlException(url, "download could not start, wrong username ('" + settings.getUsername() + "')?");
    }
}
