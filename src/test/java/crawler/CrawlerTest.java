package crawler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.crawler.SiteUrl;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.crawler.Crawler;

public class CrawlerTest {

    public static final int PORT = 8081;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Before
    public void setup() throws IOException {
        givenPageExists("output.html");
        givenPageExists("output2.html");
    }

    private void givenPageExists(String page) throws IOException {
        givenThat(get(urlMatching("/" + page)).willReturn(aResponse()
                .withStatus(200)
                .withBody(Resources.toString(Resources.getResource(page), defaultCharset()))
                .withHeader("Content-Type", "text/html")));
    }

    @Test
    public void testVisitSinglePageWithNoLinks() throws IOException, InterruptedException {
        SiteUrl page = new Crawler("http://localhost:" + PORT).visitLink("http://localhost:" + PORT + "/output2.html");

        System.out.println(page);

        assertThat(page.getLink(), is("http://localhost:" + PORT + "/output2.html"));
        assertThat(page.getError().isPresent(), is(false));
        assertThat(page.getLinks().size(), is(0));
        assertThat(page.getExternalLinks().size(), is(0));
        assertThat(page.getImages().size(), is(3));
        assertThat(page.getScripts().size(), is(2));
        assertThat(page.getImports().size(), is(1));
    }

    @Test
    public void testVisitPageWithTwoLinksAndOneError() throws IOException, InterruptedException, ExecutionException {
        List<SiteUrl> pages = new Crawler("http://localhost:" + PORT).crawl("http://localhost:" + PORT + "/output.html");

        assertThat(pages.get(0).getLink(), is("http://localhost:" + PORT + "/output.html"));
        assertThat(pages.get(0).getError().isPresent(), is(false));
        assertThat(pages.get(0).getLinks().size(), is(2));
        assertThat(pages.get(0).getExternalLinks().size(), is(1));
        assertThat(pages.get(0).getImages().size(), is(1));
        assertThat(pages.get(0).getScripts().size(), is(0));
        assertThat(pages.get(0).getImports().size(), is(0));

        assertThat(pages.get(1).getLink(), is("http://localhost:" + PORT + "/output2.html"));
        assertThat(pages.get(1).getError().isPresent(), is(false));
        assertThat(pages.get(1).getLinks().size(), is(0));
        assertThat(pages.get(1).getExternalLinks().size(), is(0));
        assertThat(pages.get(1).getImages().size(), is(3));
        assertThat(pages.get(1).getScripts().size(), is(2));
        assertThat(pages.get(1).getImports().size(), is(1));

    }
}
