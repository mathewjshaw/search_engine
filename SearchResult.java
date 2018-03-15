public class SearchResult implements Comparable<SearchResult> {
	private int frequency;
	private int initialPosition;
	private final String path;

	/**
	 * Creates a search result object with given parameters frequency,
	 * initialPosition, path
	 * 
	 * @param frequency
	 * @param initialPosition
	 * @param path
	 */
	public SearchResult(int frequency, int initialPosition, String path) {
		this.frequency = frequency;
		this.initialPosition = initialPosition;
		this.path = path;
	}

	/**
	 * Compares search result by frequency, initial position, then path
	 */
	@Override
	public int compareTo(SearchResult other) {
		if (frequency == other.frequency) {
			if (initialPosition == other.initialPosition) {
				return path.compareTo(other.path);
			}
			return Integer.compare(initialPosition, other.initialPosition);
		}
		return Integer.compare(other.frequency, frequency);
	}

	/**
	 * Updates the SearchResult, adding newFrequency and replacing initial
	 * position
	 * 
	 * @param newFrequency
	 * @param newPosition
	 */
	public void update(int newFrequency, int newPosition) {
		frequency += newFrequency;

		if (newPosition < initialPosition) {
			initialPosition = newPosition;
		}
	}

	/**
	 * Returns string representation of path
	 * 
	 * @return
	 */
	public String getPath() {
		return path.toString();
	}

	/**
	 * Returns string representation of frequency
	 * 
	 * @return
	 */
	public String getFrequency() {
		return String.valueOf(frequency);
	}

	/**
	 * Returns string representation of initial position
	 * 
	 * @return
	 */
	public String getInitialPosition() {
		return String.valueOf(initialPosition);
	}

}