import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;

public class WebCrawler {
	private final WorkQueue queue;
	private final InvertedIndexThreadSafe index;
	private int max;
	private HashSet<URL> urls;

	public WebCrawler(WorkQueue queue, InvertedIndex index, HashSet<URL> urls) {
		this.queue = queue;
		this.index = new InvertedIndexThreadSafe(index);
		this.urls = urls;
		max = 0;
	}

	public void crawl(URL seed, int limit) {
		synchronized (urls) {
			max += limit;
			urls.add(seed);
			queue.execute(new WebMinion(seed));
		}
		queue.finish();
	}

	private class WebMinion implements Runnable {
		private URL url;

		public WebMinion(URL url) {
			this.url = url;
		}

		@Override
		public void run() {

			try {
				String html = HTTPFetcher.fetchHTML(url.toString());
				synchronized (urls) {
					for (URL link : LinkParser.listLinks(url, html)) {
						if (urls.size() < max && !urls.contains(link)) {
							System.out.println("size: " + urls.size());
							urls.add(link);
							queue.execute(new WebMinion(link));
						}
					}
				}
				index.addAll(WordParser.parseWords(HTMLCleaner.stripHTML(html)), url.toString());
			} catch (UnknownHostException e) {
				System.out.println("ISSUE WITH URL, PLEASE TRY AGAIN");
			} catch (MalformedURLException e) {
				System.out.println("ISSUE WITH URL, PLEASE TRY AGAIN");
			} catch (IOException e) {
				System.out.println("ISSUE WITH URL, PLEASE TRY AGAIN");
			}
		}
	}
}