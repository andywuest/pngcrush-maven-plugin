package de.jos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

/**
 * Walker that walks the directory and handles all the png files.
 * 
 * @author andy
 * 
 * @param <E>
 */
public class PngCrushDirectoryWalker<E> extends DirectoryWalker<E> {

	private final boolean dryRun;

	private final String pngCrushExecutable;

	private final List<String> arguments;

	private final String sourceDirectory;

	private final Log log;

	public PngCrushDirectoryWalker(final Log log, boolean dryRun, String pngCrushExecutable, List<String> arguments,
			String sourceDirectory) {
		this.log = log;
		this.dryRun = dryRun;
		this.pngCrushExecutable = pngCrushExecutable;
		this.arguments = arguments;
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleFile(File sourceFile, int depth, Collection<E> results) throws IOException {

		final String tempDirPrefix = System.getProperty("java.io.tmpdir") + File.separator;

		if (sourceFile.getName().endsWith(".png")) {
			// get and output the filename relative to the sourceDirectory
			String outputFileName = sourceFile.toString();
			outputFileName = outputFileName.substring(getSourceDirectory().length() + 1).replace('\\', '/');

			log.info(outputFileName);

			// target file name in temp directory
			final File targetFile = new File(tempDirPrefix + sourceFile.getName());

			// delete the target file if it already exists
			FileUtils.deleteQuietly(targetFile);

			// construct command
			final StringBuilder command = new StringBuilder();

			// check if we need to create a shell
			command.append(isWindows() ? "cmd /c " : "");
			// append the executable
			command.append(getPngCrushExecutable()).append(" ");

			// add arguments
			for (String tmpArgument : getArguments()) {
				command.append(" ").append(tmpArgument);
			}

			// append the source and target file name
			command.append(" ").append(sourceFile);
			command.append(" ").append(targetFile);

			// run pngcrush
			final Process p = Runtime.getRuntime().exec(command.toString());

			// read the output
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
			}

			// check the old/new sizes
			long oldSize = FileUtils.sizeOf(sourceFile);
			long newSize = FileUtils.sizeOf(targetFile);

			double newSizePercent = (100.0 * newSize / oldSize);

			final StringBuilder sbResult = new StringBuilder();
			sbResult.append(oldSize + " => " + newSize);
			sbResult.append(", ").append(new DecimalFormat("#.##").format(newSizePercent)).append("%");

			// if the new size is >= old size, image is already optimized
			if (newSize >= oldSize) {
				sbResult.append("  => OK ");
				log.info(sbResult.toString());
			} else {
				// if dry run show only the results
				if (isDryRun()) {
					FileUtils.deleteQuietly(targetFile);
					sbResult.append(oldSize > newSize ? "  => can be optimized !!!" : "");
					log.info(sbResult.toString());
				} else {
					// if not dry run
					// delete the old file
					FileUtils.deleteQuietly(sourceFile);
					// and replace it with the crushed one
					FileUtils.moveFile(targetFile, sourceFile);
					// print result
					sbResult.append(oldSize > newSize ? "  => was optimized !!!" : "");
					log.info(sbResult.toString());
				}
			}
		}
	}

	public void walk() {
		try {
			walk(new File(getSourceDirectory()), new ArrayList<E>());
		} catch (IOException e) {
			log.info("Walk the directory tree failed with error : " + e.getMessage());
		}
	}

	private static boolean isWindows() {
		final String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);

	}

	/**
	 * Getter for dryRun.
	 * 
	 * @return the dryRun
	 */
	private boolean isDryRun() {
		return dryRun;
	}

	/**
	 * Getter for pngCrushExecutable.
	 * 
	 * @return the pngCrushExecutable
	 */
	private String getPngCrushExecutable() {
		return pngCrushExecutable;
	}

	/**
	 * Getter for arguments.
	 * 
	 * @return the arguments
	 */
	private List<String> getArguments() {
		return arguments;
	}

	/**
	 * Getter for sourceDirectory.
	 * 
	 * @return the sourceDirectory
	 */
	private String getSourceDirectory() {
		return sourceDirectory;
	}

}
