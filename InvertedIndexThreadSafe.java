import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class InvertedIndexThreadSafe extends InvertedIndex {
	private ReadWriteLock lock;

	/**
	 * Initializes the inverted index.
	 */
	public InvertedIndexThreadSafe() {
		super();
		lock = new ReadWriteLock();
	}

	public InvertedIndexThreadSafe(InvertedIndex other) {
		super(other);
		lock = new ReadWriteLock();
	}

	/**
	 * Helps us catch bugs
	 */
	@Override
	public String toString() {
		lock.lockReadOnly();
		try {
			return super.toString();
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Adds word, filename, and position to inverted index
	 *
	 * @param word
	 * @param filename
	 * @param position
	 */
	@Override
	public void add(String word, String filename, Integer position) {
		lock.lockReadWrite();
		try {
			super.add(word, filename, position);
		} finally {
			lock.unlockReadWrite();
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
	@Override
	public void addAll(String[] words, String filename, int start) {
		lock.lockReadWrite();
		try {
			super.addAll(words, filename, start);
		} finally {
			lock.unlockReadWrite();
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
	@Override
	public void addAll(InvertedIndex other) {
		lock.lockReadWrite();
		try {
			super.addAll(other);
		} finally {
			lock.unlockReadWrite();
		}

	}

	/**
	 * Writes inverted index as JSON to specified path
	 * 
	 * @param path
	 * @throws IOException
	 */
	@Override
	public void asJSON(Path path) throws IOException {
		lock.lockReadOnly();
		try {
			super.asJSON(path);
		} finally {
			lock.unlockReadWrite();
		}
	}

	/**
	 * Returns the number of files in which a word was found (i.e. the number of
	 * files associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	@Override
	public int count(String word) {
		lock.lockReadOnly();
		try {
			return super.count(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Returns the number of times a word was found in the specified file (i.e.
	 * the number of positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	@Override
	public int count(String word, String filename) {
		lock.lockReadOnly();
		try {
			return super.count(word, filename);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	@Override
	public boolean contains(String word) {
		lock.lockReadOnly();
		try {
			return super.contains(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Tests whether the index contains the specified word & filename.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	@Override
	public boolean contains(String word, String filename) {
		lock.lockReadOnly();
		try {
			return super.contains(word, filename);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Takes in an array of query words and searches the index for exact matches
	 * 
	 * @param queryWords
	 * @return
	 */
	@Override
	public ArrayList<SearchResult> exactSearch(String[] queryWords) {
		lock.lockReadOnly();
		try {
			return super.exactSearch(queryWords);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Takes in an array of query words and searches the index for partial
	 * matches
	 * 
	 * @param queryWords
	 * @return
	 */
	@Override
	public ArrayList<SearchResult> partialSearch(String[] queryWords) {
		lock.lockReadOnly();
		try {
			return super.partialSearch(queryWords);
		} finally {
			lock.unlockReadOnly();
		}
	}

}
