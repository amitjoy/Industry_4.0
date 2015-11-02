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
package de.tum.in.osgi.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a helper class to parse manifest header entries.
 *
 * @author AMIT KUMAR MONDAL
 */
public class ManifestHeader {

	/**
	 * A header can have several entries separated by comma.
	 */
	public interface Entry {

		/**
		 * The attributes specified for this entry.
		 */
		NameValuePair[] getAttributes();

		String getAttributeValue(String name);

		/**
		 * The directives for this entry.
		 */
		NameValuePair[] getDirectives();

		String getDirectiveValue(String name);

		/**
		 * The value of the entry.
		 */
		String getValue();
	}

	/**
	 * Directives and attributes are simple name/value pairs.
	 */
	public final static class NameValuePair {

		private final String name;
		private final String value;

		public NameValuePair(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return this.name;
		}

		public String getValue() {
			return this.value;
		}
	}

	protected static final class PathImpl implements ManifestHeader.Entry {

		private NameValuePair[] attributes;

		private NameValuePair[] directives;
		private final String value;

		public PathImpl(final String path) {
			this.value = path;
		}

		/**
		 * @see org.apache.sling.commons.osgi.ManifestHeader.Entry#getAttributes()
		 */
		@Override
		public NameValuePair[] getAttributes() {
			return this.attributes;
		}

		@Override
		public String getAttributeValue(final String name) {
			String v = null;
			int index = 0;
			while ((v == null) && (index < this.attributes.length)) {
				if (this.attributes[index].getName().equals(name)) {
					v = this.attributes[index].getValue();
				}
				index++;
			}
			return v;
		}

		/**
		 * @see org.apache.sling.commons.osgi.ManifestHeader.Entry#getDirectives()
		 */
		@Override
		public NameValuePair[] getDirectives() {
			return this.directives;
		}

		@Override
		public String getDirectiveValue(final String name) {
			String v = null;
			int index = 0;
			while ((v == null) && (index < this.directives.length)) {
				if (this.directives[index].getName().equals(name)) {
					v = this.directives[index].getValue();
				}
				index++;
			}
			return v;
		}

		/**
		 * @see org.apache.sling.commons.osgi.ManifestHeader.Entry#getValue()
		 */
		@Override
		public String getValue() {
			return this.value;
		}

		public void init(final NameValuePair[] dirs, final NameValuePair[] attrs) {
			this.directives = dirs;
			this.attributes = attrs;
		}
	}

	private static final String ATTRIBUTE_SEPARATOR = "=";

	private static final int CHAR = 1;

	private static final String CLASS_PATH_SEPARATOR = ",";
	private static final int DELIMITER = 2;
	private static final String DIRECTIVE_SEPARATOR = ":=";
	private static final int ENDQUOTE = 8;

	private static final String PACKAGE_SEPARATOR = ";";

	private static final int STARTQUOTE = 4;

	/**
	 * Parse headers Like this: path; path; dir1:=dirval1; dir2:=dirval2;
	 * attr1=attrval1; attr2=attrval2, path; path; dir1:=dirval1; dir2:=dirval2;
	 * attr1=attrval1; attr2=attrval2 The returned object maintains the order of
	 * entries (paths), directives and attributes.
	 */
	public static ManifestHeader parse(final String header) {
		final ManifestHeader entry = new ManifestHeader();

		if (header != null) {
			if (header.length() == 0) {
				throw new IllegalArgumentException("A header cannot be an empty string.");
			}

			final String[] clauseStrings = parseDelimitedString(header, CLASS_PATH_SEPARATOR);
			if (clauseStrings != null) {
				for (final String clause : clauseStrings) {
					entry.add(parseStandardHeaderClause(clause));
				}
			}
		}

		return (entry.getEntries().length == 0) ? null : entry;
	}

	/**
	 * Parses delimited string and returns an array containing the tokens. This
	 * parser obeys quotes, so the delimiter character will be ignored if it is
	 * inside of a quote. This method assumes that the quote character is not
	 * included in the set of delimiter characters.
	 *
	 * @param value
	 *            the delimited string to parse.
	 * @param delim
	 *            the characters delimiting the tokens.
	 * @return an array of string tokens or null if there were no tokens.
	 **/
	private static String[] parseDelimitedString(String value, final String delim) {
		if (value == null) {
			value = "";
		}

		final List<String> list = new ArrayList<String>();

		final StringBuilder sb = new StringBuilder();

		int expecting = (CHAR | DELIMITER | STARTQUOTE);

		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);

			final boolean isDelimiter = (delim.indexOf(c) >= 0);
			final boolean isQuote = (c == '"');

			if (isDelimiter && ((expecting & DELIMITER) > 0)) {
				list.add(sb.toString().trim());
				sb.delete(0, sb.length());
				expecting = (CHAR | DELIMITER | STARTQUOTE);
			} else if (isQuote && ((expecting & STARTQUOTE) > 0)) {
				sb.append(c);
				expecting = CHAR | ENDQUOTE;
			} else if (isQuote && ((expecting & ENDQUOTE) > 0)) {
				sb.append(c);
				expecting = (CHAR | STARTQUOTE | DELIMITER);
			} else if ((expecting & CHAR) > 0) {
				sb.append(c);
			} else {
				throw new IllegalArgumentException("Invalid delimited string: " + value);
			}
		}

		if (sb.length() > 0) {
			list.add(sb.toString().trim());
		}

		if (list.size() == 0) {
			return null;
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Parse a clause Like this: path; path; dir1:=dirval1; dir2:=dirval2;
	 * attr1=attrval1; attr2=attrval2
	 */
	private static ManifestHeader.Entry[] parseStandardHeaderClause(final String clauseString)
			throws IllegalArgumentException {
		// Break string into semi-colon delimited pieces.
		final String[] pieces = parseDelimitedString(clauseString, PACKAGE_SEPARATOR);

		// Count the number of different paths; paths
		// will not have an '=' in their string. This assumes
		// that paths come first, before directives and
		// attributes.
		int pathCount = 0;
		for (final String piece : pieces) {
			if (piece.indexOf('=') >= 0) {
				break;
			}
			pathCount++;
		}

		// Error if no paths were specified.
		if (pathCount == 0) {
			throw new IllegalArgumentException("No paths specified in header: " + clauseString);
		}

		// Create an array of paths.
		final PathImpl[] paths = new PathImpl[pathCount];
		for (int i = 0; i < pathCount; i++) {
			paths[i] = new PathImpl(pieces[i]);
		}

		// Parse the directives/attributes
		// and keep the order
		// for simpliefied checking if a directive/attribute is used twice, we
		// keep
		// two collections: one for the values and one for the names
		final List<ManifestHeader.NameValuePair> dirsList = new ArrayList<ManifestHeader.NameValuePair>();
		final Set<String> dirsNames = new HashSet<String>();
		final List<ManifestHeader.NameValuePair> attrsList = new ArrayList<ManifestHeader.NameValuePair>();
		final Set<String> attrsNames = new HashSet<String>();

		int idx = -1;
		String sep = null;
		for (int pieceIdx = pathCount; pieceIdx < pieces.length; pieceIdx++) {

			if ((idx = pieces[pieceIdx].indexOf(DIRECTIVE_SEPARATOR)) >= 0) {
				sep = DIRECTIVE_SEPARATOR;
			} else if ((idx = pieces[pieceIdx].indexOf(ATTRIBUTE_SEPARATOR)) >= 0) {
				sep = ATTRIBUTE_SEPARATOR;
			} else {
				throw new IllegalArgumentException("Not a directive/attribute: " + clauseString);
			}

			final String key = pieces[pieceIdx].substring(0, idx).trim();
			String value = pieces[pieceIdx].substring(idx + sep.length()).trim();

			// Remove quotes, if value is quoted.
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}

			// Save the directive/attribute in the appropriate array.
			if (sep.equals(DIRECTIVE_SEPARATOR)) {
				// Check for duplicates.
				if (dirsNames.contains(key)) {
					throw new IllegalArgumentException("Duplicate directive: " + key);
				}
				dirsList.add(new ManifestHeader.NameValuePair(key, value));
				dirsNames.add(key);
			} else {
				// Check for duplicates.
				if (attrsNames.contains(key)) {
					throw new IllegalArgumentException("Duplicate attribute: " + key);
				}
				attrsList.add(new ManifestHeader.NameValuePair(key, value));
				attrsNames.add(key);
			}
		}
		// Create directive array.
		final ManifestHeader.NameValuePair[] dirs = dirsList.toArray(new ManifestHeader.NameValuePair[dirsList.size()]);

		// Create attribute array.
		final ManifestHeader.NameValuePair[] attrs = attrsList
				.toArray(new ManifestHeader.NameValuePair[attrsList.size()]);

		// now set attributes and directives for each path
		for (int i = 0; i < pathCount; i++) {
			paths[i].init(dirs, attrs);
		}

		return paths;
	}

	/** The entries for this header. */
	private Entry[] entries = new Entry[0];

	/**
	 * Add new entries from parsing.
	 */
	private void add(final Entry[] paths) {
		if ((paths != null) && (paths.length > 0)) {
			final Entry[] copy = new Entry[this.entries.length + paths.length];
			System.arraycopy(this.entries, 0, copy, 0, this.entries.length);
			System.arraycopy(paths, 0, copy, this.entries.length, paths.length);
			this.entries = copy;
		}
	}

	/**
	 * Return the entries for this header.
	 */
	public Entry[] getEntries() {
		return this.entries;
	}
}