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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

/**
 * This is a simple class that implements a <tt>Dictionary</tt> from a
 * <tt>Map</tt>. The resulting dictionary is immutable.
 *
 * @author AMIT KUMAR MONDAL
 **/
public class MapToDictionary extends Dictionary<Object, Object> {
	/**
	 * Map source.
	 **/
	private Map<?, ?> map = null;

	public MapToDictionary(final Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public Enumeration<Object> elements() {
		if (this.map == null) {
			return null;
		}
		return new IteratorToEnumeration(this.map.values().iterator());
	}

	@Override
	public Object get(final Object key) {
		if (this.map == null) {
			return null;
		}
		return this.map.get(key);
	}

	@Override
	public boolean isEmpty() {
		if (this.map == null) {
			return true;
		}
		return this.map.isEmpty();
	}

	@Override
	public Enumeration<Object> keys() {
		if (this.map == null) {
			return null;
		}
		return new IteratorToEnumeration(this.map.keySet().iterator());
	}

	@Override
	public Object put(final Object key, final Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(final Object key) {
		throw new UnsupportedOperationException();
	}

	public void setSourceMap(final Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public int size() {
		if (this.map == null) {
			return 0;
		}
		return this.map.size();
	}
}