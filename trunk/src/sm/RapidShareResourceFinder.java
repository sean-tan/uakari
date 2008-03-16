package sm;

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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class RapidShareResourceFinder {
    private final Random random = new Random(System.currentTimeMillis());
    private final ExecutorService executor;
    private final Settings settings;
    private final Audit audit;
    private final List<Future> list = new ArrayList<Future>();

    public RapidShareResourceFinder(ExecutorService executor, Settings settings, Audit audit) {
        this.executor = executor;
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

    public void connect(String url, ResourceHandler... resourceHandlers) throws IOException, InvalidRapidshareUrlException, InterruptedException {
        try {
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
                                            List<WebLink> downloadLocations = new ArrayList<WebLink>() {
                                                {
                                                    WebLink[] webLinks = downloadPage.getLinks();
                                                    for (WebLink webLink : webLinks) {
                                                        if (webLink.getText().startsWith("Download via")) {
                                                            add(webLink);
                                                        }
                                                    }
                                                }
                                            };

                                            WebLink webLink = downloadLocations.get(Math.abs(random.nextInt() % downloadLocations.size()));
                                            audit.addMessage(webLink.getText());
                                            String urlString = webLink.getURLString();

                                            GetMethodWebRequest request = new GetMethodWebRequest(urlString);
                                            int total = client.getResource(request).getContentLength();

                                            long startingByte = 0;
                                            int pieceSize = total / resourceHandlers.length;
                                            long endByte = 0;
                                            for (int i = 0; i < resourceHandlers.length; i++) {
                                                endByte += pieceSize;
                                                if (i == resourceHandlers.length - 1 && isOdd(total)) {
                                                    endByte++;
                                                }
                                                ResourceHandler resourceHandler = resourceHandlers[i];
                                                resourceHandler.setTotal(total);
                                                handleStreamPart(urlString, startingByte, endByte, client, resourceHandler);
                                                startingByte = endByte;
                                            }
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
        } catch (SAXException e) {
            throw new Bug("Should not happen", e);
        }
    }

    private boolean isOdd(int total) {
        return total % 2 == 1;
    }

    private void handleStreamPart(final String urlString, final long startingByte, final long endByte, final WebClient client, final ResourceHandler resourceHandler) throws IOException, InterruptedException {
        list.add(executor.submit(new Runnable() {
            public void run() {
                try {
                    GetMethodWebRequest request = new GetMethodWebRequest(urlString);
                    String range = "bytes=" + startingByte + "-" + endByte;
                    request.setHeaderField("Range", range);
                    WebResponse webResponse = client.getResource(request);
                    InputStream inputStream = webResponse.getInputStream();
                    try {
                        resourceHandler.handleStream(inputStream, startingByte);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    } finally {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }));
    }

    public void cancel() {
        Iterator<Future> iterator = list.iterator();
        while (iterator.hasNext()) {
            Future future = iterator.next();
            future.cancel(true);
            iterator.remove();
        }
    }
}
