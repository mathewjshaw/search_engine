import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class Driver {

	/**
	 * Parses command line arguments and adds to Index
	 *
	 * @param Flags
	 *            "-path" or "-index"
	 */
	public static void main(String[] args) {
		Path inputPath;
		Path outputPath = null;
		ArgumentMap argMap = new ArgumentMap(args);
		HashSet<URL> urls = new HashSet<URL>();

		if (argMap.numFlags() == 0) {
			return;
		}

		InvertedIndex index = null;
		QueryHelperInterface query = null;
		WorkQueue queue = null;

		if (argMap.hasFlag("-threads")) {
			InvertedIndexThreadSafe threadSafe = new InvertedIndexThreadSafe();
			queue = new WorkQueue(argMap.getPositiveInteger("-threads", 5));

			index = threadSafe;
			query = new QueryHelperMultithreaded(queue, threadSafe);
		} else {
			index = new InvertedIndex();
			query = new QueryHelper(index);
		}

		if (argMap.hasFlag("-path")) {
			if (queue != null) {
				inputPath = Paths.get(argMap.getString("-path"));
				System.out.println("Starting Path = " + inputPath);

				InvertedIndexBuilderMultithreaded indexBuilderThreadSafe = new InvertedIndexBuilderMultithreaded(queue,
						(InvertedIndexThreadSafe) index, inputPath);

				try {
					indexBuilderThreadSafe.traverseDirectories(inputPath);
				} catch (IOException e) {
					System.out.println("Issue with either input/output path, make sure to enter paths correctly");

				}
			} else {
				try {
					inputPath = Paths.get(argMap.getString("-path"));
					System.out.println("Starting Path = " + inputPath);
					InvertedIndexBuilder.traverseDirectories(inputPath, index);
				} catch (InvalidPathException | NullPointerException e) {
					System.out.println("Invalid path, please try again");
				} catch (IOException e) {
					System.out.println("Issue with either input/output path, make sure to enter paths correctly");
				}
			}
		}

		if (argMap.hasFlag("-url")) {
			queue = new WorkQueue(5);
			WebCrawler crawler = new WebCrawler(queue, index, urls);
			try {
				crawler.crawl(new URL(argMap.getString("-url")), argMap.getPositiveInteger("-limit", 50));

			} catch (MalformedURLException e) {
				System.out.println("~ Invalid URL please try again ~");
			}
		}

		if (argMap.hasFlag("-index")) {
			try {
				outputPath = Paths.get(argMap.getString("-index", "index.json"));
				System.out.println("Output Path = " + outputPath);
				index.asJSON(outputPath);
			} catch (InvalidPathException e) {
				System.out.println("~ Invalid Output Path! ~");
			} catch (IOException e) {
				System.out.println("~ Issue with either input/output path, make sure to enter paths correctly ~");
			}
		}

		if (argMap.hasFlag("-query")) {
			try {
				query.parseQuery(Paths.get(argMap.getString("-query")), argMap.hasFlag("-exact"));
			} catch (IOException e) {
				System.out.println("~ Issue with either input/output path, make sure to enter paths correctly ~");
			} catch (InvalidPathException | NullPointerException e) {
				System.out.println("Invalid query path, please try again");
			}
		}

		if (argMap.hasFlag("-results")) {
			String resultsPath = argMap.getString("-results", "results.json");
			try {
				System.out.println("Results Path = " + resultsPath);
				query.toJSON(Paths.get(resultsPath));
			} catch (IOException e) {
				System.out.println("~ Issue with either input/output path, make sure to enter paths correctly ~");
			} catch (InvalidPathException | NullPointerException e) {
				System.out.println("~ Invalid results path, please try again ~");
			}
		}

		if (queue != null) {
			queue.shutdown();
		}
	}
}