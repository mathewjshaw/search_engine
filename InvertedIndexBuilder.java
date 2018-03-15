import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvertedIndexBuilder {

	/**
	 * Traverses directories and adds words to index
	 * 
	 * @param path
	 * @param index
	 * @throws IOException
	 */
	public static void traverseDirectories(Path path, InvertedIndex index) throws IOException {

		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> directory = Files.newDirectoryStream(path);) {
				for (Path subDirectory : directory) {
					traverseDirectories(subDirectory, index);
				}
			}
		}

		else if (path.toString().toLowerCase().endsWith(".html") || path.toString().endsWith(".htm")) {
			parseHTMLFile(path, index);
		}
	}

	/**
	 * Parses a HTML file given by "path" and returns an array words
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static void parseHTMLFile(Path path, InvertedIndex index) throws IOException {
		String line;
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);) {
			StringBuilder sb = new StringBuilder();

			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			line = sb.toString();
			line = HTMLCleaner.stripHTML(line);
			String[] words = WordParser.parseWords(line);

			index.addAll(words, path.toString());
		}
	}

}
