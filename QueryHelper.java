import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class QueryHelper implements QueryHelperInterface {

	private final TreeMap<String, ArrayList<SearchResult>> queryMap;
	private final InvertedIndex index;

	/**
	 * Creates a QueryHelper object attached to the given index
	 * 
	 * @param index
	 */
	public QueryHelper(InvertedIndex index) {
		this.index = index;
		queryMap = new TreeMap<String, ArrayList<SearchResult>>();
	}

	/**
	 * Converts query treeMap to JSON output using methods from JSONWriter
	 * 
	 * @param path
	 * @throws IOException
	 */
	@Override
	public void toJSON(Path path) throws IOException {
		JSONWriter.asSearchResults(queryMap, path);
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
				String[] parsedLine = (WordParser.parseWords(line.toString()));

				if (parsedLine.length == 0) {
					continue;
				}
				Arrays.sort(parsedLine);
				if (exact) {
					queryMap.put(String.join(" ", parsedLine), index.exactSearch(parsedLine));
				} else {
					queryMap.put(String.join(" ", parsedLine), index.partialSearch(parsedLine));
				}
			}
		}
	}
}
