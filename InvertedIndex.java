import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

public class InvertedIndex {
	/**
	 * An inverted index which uses TreeMap<String, TreeMap<String,
	 * TreeSet<Integer>>> Data Structure
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes the inverted index.
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
	}

	public InvertedIndex(InvertedIndex other) {
		this.index = other.index;
	}

	/**
	 * Helps us catch bugs
	 */
	@Override
	public String toString() {
		return index.toString();
	}

	/**
	 * Adds word, filename, and position to inverted index
	 *
	 * @param word
	 * @param filename
	 * @param position
	 */
	private void addHelper(String word, String filename, Integer position) {
		if (index.get(word) == null) {
			index.put(word, new TreeMap<>());
		}
		if (index.get(word).get(filename) == null) {
			index.get(word).put(filename, new TreeSet<>());
		}
		index.get(word).get(filename).add(position);
	}

	/**
	 * Adds word, filename, and position to inverted index
	 *
	 * @param word
	 * @param filename
	 * @param position
	 */
	public void add(String word, String filename, Integer position) {
		addHelper(word, filename, position);
	}

	/**
	 * Adds an array of words, filenames, and positions to inverted index
	 * 
	 * @param words
	 * @param filename
	 */

	public void addAll(String[] words, String filename) {
		addAll(words, filename, 1);
	}

	/**
	 * Adds an array of words, filenames, and positions to inverted index.
	 * Positions begin at "Start" parameter.
	 * 
	 * @param words
	 * @param filename
	 * @param start
	 */

	public void addAll(String[] words, String filename, int start) {
		for (String word : words) {
			addHelper(word, filename, start++);
		}
	}

	/**
	 * Adds an array of words, filenames, and positions to inverted index.
	 * Positions begin at "Start" parameter.
	 * 
	 * @param words
	 * @param filename
	 * @param start
	 */
	public void addAll(InvertedIndex other) {
		for (String word : other.index.keySet()) {
			if (this.index.containsKey(word) == false) {
				this.index.put(word, other.index.get(word));
			} else {
				for (String path : other.index.get(word).keySet()) {
					if (this.index.get(word).containsKey(path) == false) {
						this.index.get(word).put(path, other.index.get(word).get(path));
					} else {
						this.index.get(word).get(path).addAll(other.index.get(word).get(path));
					}

				}
			}
		}
	}

	/**
	 * Writes inverted index as JSON to specified path
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void asJSON(Path path) throws IOException {
		JSONWriter.asDoubleNestedObject(index, path);
	}

	/**
	 * Returns the number of files in which a word was found (i.e. the number of
	 * files associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int count(String word) {
		if (index.get(word) == null) {
			return 0;
		}
		return index.get(word).size();
	}

	/**
	 * Returns the number of times a word was found in the specified file (i.e.
	 * the number of positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int count(String word, String filename) {
		if (index.get(word) == null || (!index.get(word).containsKey(filename))) {
			return 0;
		}
		return index.get(word).get(filename).size();
	}

	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word) {
		if (index.containsKey(word)) {
			return true;
		}
		return false;
	}

	/**
	 * Tests whether the index contains the specified word & filename.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word, String filename) {
		if (index.containsKey(word) && (index.get(word).containsKey(filename))) {
			return true;
		}
		return false;
	}

	/**
	 * Takes in a partial query and returns an array of all the full words in
	 * the index
	 * 
	 * @param query
	 * @return
	 */
	private void searchFilenames(TreeMap<String, TreeSet<Integer>> fileMap,
			TreeMap<String, SearchResult> searchResultMap, ArrayList<SearchResult> searchResults, String queryWord) {
		for (String filename : fileMap.keySet()) {
			int newFrequency = count(queryWord, filename);
			int newPosition = index.get(queryWord).get(filename).first();

			if (searchResultMap.containsKey(filename)) {
				searchResultMap.get(filename).update(newFrequency, newPosition);
			} else {
				SearchResult searchResult = new SearchResult(newFrequency, newPosition, filename);
				searchResultMap.put(filename, searchResult);
				searchResults.add(searchResult);
			}
		}
	}

	/**
	 * Takes in an array of query words and searches the index for exact matches
	 * 
	 * @param queryWords
	 * @return
	 */
	public ArrayList<SearchResult> exactSearch(String[] queryWords) {
		ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
		TreeMap<String, SearchResult> searchResultMap = new TreeMap<String, SearchResult>();
		for (String queryWord : queryWords) {
			if (contains(queryWord)) {
				TreeMap<String, TreeSet<Integer>> fileMap = index.get(queryWord);
				searchFilenames(fileMap, searchResultMap, searchResults, queryWord);
			}
		}
		Collections.sort(searchResults);
		return searchResults;
	}

	/**
	 * Takes in an array of query words and searches the index for partial
	 * matches
	 * 
	 * @param queryWords
	 * @return
	 */
	public ArrayList<SearchResult> partialSearch(String[] queryWords) {
		ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
		TreeMap<String, SearchResult> searchResultMap = new TreeMap<String, SearchResult>();

		for (String queryWord : queryWords) {
			for (String word : index.tailMap(queryWord, true).keySet()) {
				if (!word.startsWith(queryWord)) {
					break;
				}
				TreeMap<String, TreeSet<Integer>> fileMap = index.get(word);
				searchFilenames(fileMap, searchResultMap, searchResults, word);

			}
		}
		Collections.sort(searchResults);
		return searchResults;
	}
}
