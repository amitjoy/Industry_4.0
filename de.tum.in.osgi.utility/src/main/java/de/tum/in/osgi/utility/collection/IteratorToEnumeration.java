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
package de.tum.in.osgi.utility.collection;

import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class IteratorToEnumeration implements Enumeration<Object> {
	private final Iterator<?> iter;

	public IteratorToEnumeration(final Iterator<?> iter) {
		this.iter = iter;
	}

	@Override
	public boolean hasMoreElements() {
		if (this.iter == null) {
			return false;
		}
		return this.iter.hasNext();
	}

	@Override
	public Object nextElement() {
		if (this.iter == null) {
			return null;
		}
		return this.iter.next();
	}
}