import java.io.IOException;
import java.nio.file.Path;

public interface QueryHelperInterface {
	/**
	 * Converts query treeMap to JSON output using methods from JSONWriter
	 *
	 * @param path
	 * @throws IOException
	 */
	public void toJSON(Path path) throws IOException;

	/**
	 * Takes a path of a file to parse and returns a parsed list of arrays
	 * containing query words
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public void parseQuery(Path filename, boolean exact) throws IOException;
}
