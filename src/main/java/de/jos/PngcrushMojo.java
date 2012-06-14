package de.jos;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal pngcrush
 */
public class PngcrushMojo extends AbstractMojo {

	/**
	 * Source directory that is to be checked for png files.
	 * 
	 * @parameter expression="${basedir}/src"
	 * @required
	 */
	private String sourceDirectory;

	/**
	 * Just perform a dry run that shows the possible compression ratios.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean dryRun;

	/**
	 * Path to the pngcrush executable.
	 * 
	 * @parameter
	 */
	private String pathToPngcrush;

	/**
	 * pngcrush arguments.
	 * 
	 * @parameter
	 */
	private List<String> arguments;

	public void execute() throws MojoExecutionException {
		final PngCrushDirectoryWalker<Object> walker = new PngCrushDirectoryWalker<Object>(getLog(), dryRun,
				pathToPngcrush, arguments, sourceDirectory);
		walker.walk();
	}

}
