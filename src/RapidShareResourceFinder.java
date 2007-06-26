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

import java.io.InputStream;

class RapidShareResourceFinder {
    private final Settings settings;

    public RapidShareResourceFinder(Settings settings) {
        this.settings = settings;
    }

    private boolean hasParameter(String name, WebForm form) {
        String[] strings = form.getParameterNames();
        for (String string : strings) {
            if (string.equals(name))
                return true;
        }
        return false;
    }

    public static void checkUrlValidity(String url) throws InvalidRapidshareUrlException {
        try {
            WebClient client = new WebConversation();
            ClientProperties properties = client.getClientProperties();
            properties.setAcceptCookies(true);
            properties.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8 ) Gecko/20051111 Firefox/1.5");
            properties.setAutoRedirect(true);

            WebResponse webResponse = client.getResponse(new GetMethodWebRequest(url));

            String fileNotFound = "File not found";
            if (webResponse.getText().contains(fileNotFound))
                throw new InvalidRapidshareUrlException(url, fileNotFound);

            String deltedFile = "This file has been deleted";
            if (webResponse.getText().contains(deltedFile)) {
                throw new InvalidRapidshareUrlException(url, deltedFile);
            }
        } catch (HttpNotFoundException e) {
            throw new InvalidRapidshareUrlException(url, "404 - file not found");
        } catch (InvalidRapidshareUrlException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRapidshareUrlException(url, e);
        }
    }

    public void connect(String url, ResourceHandler resourceHandler) throws Exception {
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
                                        WebResponse downloadPage = downloadForm.submit();
                                        WebLink[] webLinks = downloadPage.getLinks();
                                        for (WebLink webLink : webLinks) {
                                            if (webLink.getText().startsWith("Download via")) {
                                                WebResponse theData = webLink.click();
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
            }
        }

        throw new InvalidRapidshareUrlException(url, "download could not start, wrong username?");
    }
}
