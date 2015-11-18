/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.client.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * MQTT Message GZip Encoder
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class GZipUtil {

	public static byte[] compress(final byte[] source) throws IOException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipos = null;
		try {
			gzipos = new GZIPOutputStream(baos);
			gzipos.write(source);
		} catch (final IOException e) {
			throw e;
		} finally {
			if (gzipos != null) {
				try {
					gzipos.close();
				} catch (final IOException e) {
					// Ignore
				}
			}
		}
		return baos.toByteArray();
	}

	public static byte[] decompress(final byte[] source) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ByteArrayInputStream bais = new ByteArrayInputStream(source);
		GZIPInputStream gzipis = null;

		try {
			gzipis = new GZIPInputStream(bais);

			int n;
			final int MAX_BUF = 1024;
			final byte[] buf = new byte[MAX_BUF];
			while ((n = gzipis.read(buf, 0, MAX_BUF)) != -1) {
				baos.write(buf, 0, n);
			}
		} catch (final IOException e) {
			throw e;
		} finally {
			if (gzipis != null) {
				try {
					gzipis.close();
				} catch (final IOException e) {
					// Ignore
				}
			}

			try {
				baos.close();
			} catch (final IOException e) {
				// Ignore
			}
		}

		return baos.toByteArray();
	}

	public static boolean isCompressed(final byte[] bytes) throws IOException {
		if ((bytes == null) || (bytes.length < 2)) {
			return false;
		} else {
			return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
					&& (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
		}
	}
}
