import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;

public class QueryHelperMultithreaded implements QueryHelperInterface {
	public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
	private final TreeMap<String, ArrayList<SearchResult>> queryMap;
	private final InvertedIndexThreadSafe indexThreaded;
	private final WorkQueue minions;
	private final ReadWriteLock lock;

	/**
	 * Creates a QueryHelper object attached to the given index
	 * 
	 * @param index
	 */
	public QueryHelperMultithreaded(WorkQueue workQueue, InvertedIndexThreadSafe indexThreaded) {
		minions = workQueue;
		this.indexThreaded = indexThreaded;
		queryMap = new TreeMap<String, ArrayList<SearchResult>>();
		lock = new ReadWriteLock();
	}

	/**
	 * Converts query treeMap to JSON output using methods from JSONWriter
	 *
	 * @param path
	 * @throws IOException
	 */
	@Override
	public void toJSON(Path path) throws IOException {
		lock.lockReadOnly();
		JSONWriter.asSearchResults(queryMap, path);
		lock.unlockReadOnly();
	}

	/**
	 * Helper method which is run by SearchMinions to add the search results to
	 * queryMap
	 * 
	 * @param query
	 * @param results
	 */

	private void addResults(String query, ArrayList<SearchResult> results) {
		lock.lockReadWrite();
		queryMap.put(query, results);
		lock.unlockReadWrite();
	}

	/**
	 * Takes a path of a file to parse and returns a parsed list of arrays
	 * containing query words
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	@Override
	public void parseQuery(Path filename, boolean exact) throws IOException {
		String line;
		try (BufferedReader br = Files.newBufferedReader(filename, StandardCharsets.UTF_8);) {
			while ((line = br.readLine()) != null) {
				minions.execute(new SearchMinion(line, exact, indexThreaded));
			}
		}
		minions.finish();
	}

	/**
	 * Handles searching individual queries and adding results to the queryMap
	 * 
	 * @param String[]
	 *            parsedLine, boolean exact, InvertedIndexThreadSafe
	 *            indexThreaded
	 */
	private class SearchMinion implements Runnable {
		private String line;
		private InvertedIndexThreadSafe indexThreaded;
		private boolean exact;

		public SearchMinion(String line, boolean exact, InvertedIndexThreadSafe indexThreaded) {
			logger.debug("Search Minion created");
			this.line = line;
			this.indexThreaded = indexThreaded;
			this.exact = exact;
		}

		@Override
		public void run() {
			String[] parsedLine = (WordParser.parseWords(line.toString()));
			if (parsedLine.length == 0) {
				return;
			}
			Arrays.sort(parsedLine);

			ArrayList<SearchResult> tempArray = (exact) ? indexThreaded.exactSearch(parsedLine)
					: indexThreaded.partialSearch(parsedLine);

			line = String.join(" ", parsedLine);
			addResults(line, tempArray);
			logger.debug("Search Minion finished {}");
		}
	}
}