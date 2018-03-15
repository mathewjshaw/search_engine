import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

public class JSONWriter {

	/**
	 * Returns a String with the specified number of tab characters.
	 *
	 * @param times
	 *            number of tab characters to include
	 * @return tab characters repeated the specified number of times
	 */
	public static String indent(int times) {
		char[] tabs = new char[times];
		Arrays.fill(tabs, '\t');
		return String.valueOf(tabs);
	}

	/**
	 * Returns a quoted version of the provided text.
	 *
	 * @param text
	 *            text to surround in quotes
	 * @return text surrounded by quotes
	 */
	public static String quote(String text) {
		return String.format("\"%s\"", text);
	}

	/**
	 * Writes the set of elements as a JSON array at the specified indent level.
	 *
	 * @param writer
	 *            writer to use for output
	 * @param elements
	 *            elements to write as JSON array
	 * @param level
	 *            number of times to indent the array itself
	 * @throws IOException
	 */
	private static void asArray(Writer writer, TreeSet<Integer> elements, int level) throws IOException {
		writer.write("[" + "\n");
		if (elements.isEmpty() == false) {
			for (Integer elem : elements.headSet(elements.last())) {
				writer.write(indent(level) + elem + "," + "\n");
			}
		}
		writer.write(indent(level) + elements.last() + "\n");
		writer.write(indent(level - 1) + "]");
	}

	/**
	 * Writes the set of elements as a JSON array to the path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asArray(TreeSet<Integer> elements, Path path) throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(writer, elements, 1);
			writer.flush();
		}

	}

	/**
	 * Writes the map of elements as a JSON object to the path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asObject(TreeMap<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {

			writer.write("{" + "\n");
			if (elements.isEmpty() == false) {
				for (String key : elements.headMap(elements.lastKey()).keySet()) {
					Integer value = elements.get(key);
					writer.write(indent(1) + "\"" + key + "\"" + ": " + value + "," + "\n");
				}
			}
			writer.write(indent(1) + "\"" + elements.lastKey() + "\"" + ": " + elements.get(elements.lastKey()) + "\n");
			writer.write("}");
			writer.flush();
		}
	}

	/**
	 * Writes the set of elements as a JSON object with a nested array to the
	 * path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object with a nested array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asNestedObject(TreeMap<String, TreeSet<Integer>> elements, Writer writer) throws IOException {
		if (elements.isEmpty() == false) {
			for (String filename : elements.headMap(elements.lastKey()).keySet()) {
				TreeSet<Integer> tree = elements.get(filename);
				writer.write(indent(2) + "\"" + filename + "\"" + ": ");
				asArray(writer, tree, 3);
				writer.write("," + "\n");
			}
		}
		writer.write(indent(2) + "\"" + elements.lastKey() + "\"" + ": ");
		asArray(writer, elements.get(elements.lastKey()), 3);
		writer.write("\n");
	}

	/**
	 * Writes the set of elements as a JSON object with a nested treeMap to the
	 * path using UTF8.
	 *
	 * @param index
	 * @param path
	 * @throws IOException
	 */
	public static void asDoubleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("{" + "\n");
			if (index.size() > 1) {
				for (String word : index.headMap(index.lastKey()).keySet()) {
					TreeMap<String, TreeSet<Integer>> treeMap = index.get(word);
					writer.write(indent(1) + "\"" + word + "\"" + ": {" + "\n");

					asNestedObject(treeMap, writer);

					writer.write(indent(1) + "}," + "\n");
				}
			}
			if (index.size() > 0) {
				writer.write(indent(1) + "\"" + index.lastKey() + "\"" + ": {" + "\n");
				asNestedObject(index.get(index.lastKey()), writer);
			}
			writer.write(indent(1) + "}" + "\n");
			writer.write("}");
		}
	}

	private static void asSearchArray(BufferedWriter writer, SearchResult searchResult) throws IOException {
		writer.write(indent(3) + "{" + "\n");
		writer.write(indent(4) + "\"" + "where" + "\"" + ": " + "\"" + searchResult.getPath() + "\"" + "," + "\n");
		writer.write(indent(4) + "\"" + "count" + "\"" + ": " + searchResult.getFrequency() + "," + "\n");
		writer.write(indent(4) + "\"" + "index" + "\"" + ": " + searchResult.getInitialPosition() + "\n");
		writer.write(indent(3) + "}");
	}

	/**
	 * Writes the set of elements as a JSON object with a nested arrayList to
	 * the path using UTF8.
	 *
	 * @param index
	 * @param path
	 * @throws IOException
	 */
	public static void asSearchResults(TreeMap<String, ArrayList<SearchResult>> queryMap, Path path)
			throws IOException {
		String queries = "\"" + "queries" + "\"" + ": ";
		String results = "\"" + "results" + "\"" + ": ";

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("[" + "\n");

			for (String query : queryMap.keySet()) {
				writer.write(indent(1) + "{" + "\n" + indent(2) + queries + "\"" + query + "\"" + "," + "\n");
				writer.write(indent(2) + results + "[" + "\n");
				if (!queryMap.get(query).isEmpty()) {
					if (queryMap.get(query).size() > 1) {
						for (SearchResult searchResult : queryMap.get(query).subList(0,
								queryMap.get(query).size() - 1)) {
							asSearchArray(writer, searchResult);
							writer.write("," + "\n");
						}
					}
					asSearchArray(writer, queryMap.get(query).get(queryMap.get(query).size() - 1));
					writer.write("\n");
				}
				writer.write(indent(2) + "]" + "\n" + indent(1) + "}");
				if (!query.equals(queryMap.lastKey())) {
					writer.write(",");
				}
				writer.write("\n");
			}
			writer.write("]");
		}
	}
}
