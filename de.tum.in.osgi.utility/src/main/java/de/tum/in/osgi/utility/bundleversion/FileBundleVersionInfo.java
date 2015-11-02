/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.osgi.utility.bundleversion;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * BundleVersionInfo based on a bundle jar file
 *
 * @author AMIT KUMAR MONDAL
 */
public class FileBundleVersionInfo extends BundleVersionInfo<File> {

	private final boolean isSnapshot;
	private final long lastModified;
	private final File source;
	private final String symbolicName;
	private final Version version;

	public FileBundleVersionInfo(final File bundle) throws IOException {
		this.source = bundle;
		final JarFile f = new JarFile(bundle);
		try {
			final Manifest m = f.getManifest();
			if (m == null) {
				this.symbolicName = null;
				this.version = null;
				this.isSnapshot = false;
				this.lastModified = BND_LAST_MODIFIED_MISSING;
			} else {
				this.symbolicName = m.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
				final String v = m.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
				this.version = v == null ? null : new Version(v);
				this.isSnapshot = (v != null) && v.contains(SNAPSHOT_MARKER);
				final String last = m.getMainAttributes().getValue(BND_LAST_MODIFIED);
				long lastMod = BND_LAST_MODIFIED_MISSING;
				if (last != null) {
					try {
						lastMod = Long.parseLong(last);
					} catch (final NumberFormatException ignore) {
					}
				}
				this.lastModified = lastMod;
			}
		} finally {
			f.close();
		}
	}

	public long getBundleLastModified() {
		return this.lastModified;
	}

	public String getBundleSymbolicName() {
		return this.symbolicName;
	}

	public File getSource() {
		return this.source;
	}

	public Version getVersion() {
		return this.version;
	}

	public boolean isBundle() {
		return this.symbolicName != null;
	}

	public boolean isSnapshot() {
		return this.isSnapshot;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.source.getAbsolutePath();
	}
}