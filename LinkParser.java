import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LinkParser {

	// https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a
	// https://docs.oracle.com/javase/tutorial/networking/urls/creatingUrls.html
	// https://developer.mozilla.org/en-US/docs/Learn/Common_questions/What_is_a_URL

	/**
	 * Removes the fragment component of a URL (if present), and properly
	 * encodes the query string (if necessary).
	 *
	 * @param url
	 *            url to clean
	 * @return cleaned url (or original url if any issues occurred)
	 */
	public static URL clean(URL url) {
		try {
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), null).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return url;
		}
	}

	/**
	 * Fetches the HTML (without any HTTP headers) for the provided URL. Will
	 * return null if the link does not point to a HTML page.
	 *
	 * @param url
	 *            url to fetch HTML from
	 * @return HTML as a String or null if the link was not HTML
	 */
	public static String fetchHTML(URL url) {
		String html = null;
		try {
			html = HTTPFetcher.fetchHTML(url.toString());
		} catch (IOException e) {
			System.out.println("Issue with fetching HTML");
		}
		return html;
	}

	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of
	 * the anchor tags in the provided HTML. The links will be converted to
	 * absolute using the base URL and cleaned (removing fragments and encoding
	 * special characters as necessary).
	 *
	 * @param base
	 *            base url used to convert relative links to absolute3
	 * @param html
	 *            raw html associated with the base url
	 * @return cleaned list of all http(s) links in the order they were found
	 */
	public static ArrayList<URL> listLinks(URL base, String html) {
		ArrayList<URL> links = new ArrayList<URL>();
		base = clean(base);

		Pattern pattern = Pattern.compile("<a[^>]*\\s*href\\s*=\\s*\"\\s*(.*?)\\s*\"\\s*", Pattern.CASE_INSENSITIVE);
		java.util.regex.Matcher matcher = pattern.matcher(html);

		while (matcher.find()) {

			try {
				URL tempUrl = clean(new URL(base, matcher.group(1)));
				if (tempUrl.getProtocol().toLowerCase().startsWith("http")) {
					links.add(tempUrl);
				}
			} catch (MalformedURLException e) {
				System.out.println("~ Issue with URL ~");
			}
		}
		return links;
	}
}
