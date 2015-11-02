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

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * BundleVersionInfo based on a Bundle object
 * 
 * @author AMIT KUMAR MONDAL
 */
public class BundleBundleVersionInfo extends BundleVersionInfo<Bundle> {

	private final long lastModified;
	private final Bundle source;

	public BundleBundleVersionInfo(final Bundle b) {
		this.source = b;

		long lastMod = BND_LAST_MODIFIED_MISSING;
		final String mod = this.source.getHeaders().get(BND_LAST_MODIFIED);
		if (mod != null) {
			try {
				lastMod = Long.parseLong(mod);
			} catch (final NumberFormatException ignore) {
			}
		}
		this.lastModified = lastMod;
	}

	@Override
	public long getBundleLastModified() {
		return this.lastModified;
	}

	@Override
	public String getBundleSymbolicName() {
		return this.source.getSymbolicName();
	}

	@Override
	public Bundle getSource() {
		return this.source;
	}

	@Override
	public Version getVersion() {
		final String versionInfo = this.source.getHeaders().get(Constants.BUNDLE_VERSION);
		return (versionInfo == null ? null : new Version(versionInfo));
	}

	@Override
	public boolean isBundle() {
		return true;
	}

	@Override
	public boolean isSnapshot() {
		return this.getVersion().toString().contains(SNAPSHOT_MARKER);
	}
}