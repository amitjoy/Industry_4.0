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
package de.tum.in.mongodb;

import com.mongodb.Mongo;
import com.mongodb.client.MongoDatabase;

/**
 * Implementation class of {@link MongoDBService}
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class MongoDBServiceImpl implements MongoDBService {
	private final MongoDatabase m_db;
	private final Mongo m_mongo;

	/** Constructor */
	public MongoDBServiceImpl(final Mongo mongo, final MongoDatabase db) {
		this.m_mongo = mongo;
		this.m_db = db;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		this.m_mongo.close();
	}

	/** {@inheritDoc} */
	@Override
	public MongoDatabase getDatabase() {
		return this.m_db;
	}
}
