import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;

public class InvertedIndexBuilderMultithreaded {
	private final WorkQueue minions;
	private final InvertedIndexThreadSafe indexThreaded;
	public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

	/**
	 * Creates inverted index builder attached to the given work queue
	 * 
	 * @param workQueue
	 * @param indexThreaded
	 * @param startPath
	 */
	public InvertedIndexBuilderMultithreaded(WorkQueue workQueue, InvertedIndexThreadSafe indexThreaded,
			Path startPath) {
		minions = workQueue;
		this.indexThreaded = indexThreaded;
	}

	/**
	 * Calls traverseHelper method before calling minions.finish()
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void traverseDirectories(Path path) throws IOException {
		traverseHelper(path);
		minions.finish();
	}

	/**
	 * Traverses directories and adds words to index
	 * 
	 * @param path
	 * @param index
	 * @throws IOException
	 */
	private void traverseHelper(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				for (Path directory : stream) {
					traverseHelper(directory);
				}
			}

		} else if (path.toString().toLowerCase().endsWith(".html") || path.toString().endsWith(".htm")) {
			minions.execute(new DirectoryMinion(path, indexThreaded));
		}
		logger.debug("Minion finished {}", path);
	}

	/**
	 * Handles per-directory parsing. If a subdirectory is encountered, a new
	 * {@link DirectoryMinion} is created to handle that subdirectory.
	 */
	private class DirectoryMinion implements Runnable {

		private Path directory;
		private InvertedIndexThreadSafe indexThreaded;

		public DirectoryMinion(Path directory, InvertedIndexThreadSafe indexThreaded) {
			logger.debug("Minion created for {}", directory);
			this.directory = directory;
			this.indexThreaded = indexThreaded;

		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseHTMLFile(directory, local);
				indexThreaded.addAll(local);

			} catch (IOException e) {
				logger.debug(e);

			}
			logger.debug("Minion finished {}", directory);
		}

	}
}
