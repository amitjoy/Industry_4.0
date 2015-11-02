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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper around a dictionary access it as a Map
 *
 * @author AMIT KUMAR MONDAL
 */
public class DictionaryAsMap<U, V> extends AbstractMap<U, V> {

	class KeyEntry implements Map.Entry<U, V> {

		private final U key;

		KeyEntry(final U key) {
			this.key = key;
		}

		@Override
		public U getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return DictionaryAsMap.this.dict.get(this.key);
		}

		@Override
		public V setValue(final V value) {
			return DictionaryAsMap.this.put(this.key, value);
		}
	}

	private final Dictionary<U, V> dict;

	public DictionaryAsMap(final Dictionary<U, V> dict) {
		this.dict = dict;
	}

	@Override
	public Set<Entry<U, V>> entrySet() {
		return new AbstractSet<Entry<U, V>>() {
			@Override
			public Iterator<Entry<U, V>> iterator() {
				final Enumeration<U> e = DictionaryAsMap.this.dict.keys();
				return new Iterator<Entry<U, V>>() {
					private U key;

					@Override
					public boolean hasNext() {
						return e.hasMoreElements();
					}

					@Override
					public Entry<U, V> next() {
						this.key = e.nextElement();
						return new KeyEntry(this.key);
					}

					@Override
					public void remove() {
						if (this.key == null) {
							throw new IllegalStateException();
						}
						DictionaryAsMap.this.dict.remove(this.key);
					}
				};
			}

			@Override
			public int size() {
				return DictionaryAsMap.this.dict.size();
			}
		};
	}

	@Override
	public V put(final U key, final V value) {
		return this.dict.put(key, value);
	}

}