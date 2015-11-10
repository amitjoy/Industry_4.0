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
package de.tum.in.realtime.data.operation.provider;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import de.tum.in.realtime.data.operation.api.DataOperation;
import de.tum.in.realtime.data.operation.model.RealtimeData;

/**
 * Service Interface {@link DataOperation} Implementation
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(name = "de.tum.in.realtime.data.operation")
public final class DataOperationImpl implements DataOperation {

	/**
	 * MongoDB Database Collection Name
	 */
	private static final String MONGO_DB_COLLECTION_NAME = "IndustryData";

	/**
	 * MongoDB Database Name
	 */
	private static final String MONGO_DB_DATABASE_NAME = "testdb";

	/**
	 * MongoDB Server Address
	 */
	private static final String MONGO_DB_SERVER = "localhost";

	/**
	 * MongoDB Server Address Port
	 */
	private static final int MONGO_DB_SERVER_PORT = 27017;

	/**
	 * Optional MongoDB Reference
	 */
	private Optional<DB> db;

	/**
	 * Optional MongoDB Client
	 */
	private MongoClient mongo;

	/**
	 * Optional DB Collection Reference
	 */
	private Optional<DBCollection> table;

	/**
	 * Component Activation Callback
	 */
	@SuppressWarnings("deprecation")
	@Activate
	public void activate() {
		this.mongo = new MongoClient(MONGO_DB_SERVER, MONGO_DB_SERVER_PORT);
		this.db = Optional.of(this.mongo.getDB(MONGO_DB_DATABASE_NAME));
		this.table = this.db.isPresent() ? Optional.of(this.db.get().getCollection(MONGO_DB_COLLECTION_NAME))
				: Optional.empty();
	}

	/** {@inheritDoc}} */
	@Override
	public List<RealtimeData> retrieveAll() {
		final List<RealtimeData> realtimeDatas = Lists.newArrayList();
		final BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("name", "mkyong");

		final DBCursor cursor = this.table.get().find();

		while (cursor.hasNext()) {
			final DBObject object = cursor.next();
			realtimeDatas.add(this.unwrap(object));
		}
		return realtimeDatas;
	}

	/** {@inheritDoc}} */
	@Override
	public Optional<RealtimeData> retrieveById(final String id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	/** {@inheritDoc}} */
	@Override
	public boolean save(final RealtimeData realtimeData) {
		final BasicDBObject document = new BasicDBObject();
		document.put("force_x", realtimeData.force_x);
		document.put("force_y", realtimeData.force_y);
		document.put("force_z", realtimeData.force_z);
		document.put("torque_x", realtimeData.torque_x);
		document.put("torque_y", realtimeData.torque_y);
		document.put("torque_z", realtimeData.torque_z);
		document.put("time", realtimeData.time);
		this.table.get().insert(document);
		return true;
	}

	/**
	 * Unwraps {@link DBObject} to {@link RealtimeData}
	 */
	private RealtimeData unwrap(final DBObject object) {
		final RealtimeData data = new RealtimeData();
		data.force_x = object.get("force_x").toString();
		data.force_y = object.get("force_y").toString();
		data.force_z = object.get("force_z").toString();
		data.torque_x = object.get("torque_x").toString();
		data.torque_y = object.get("torque_y").toString();
		data.torque_z = object.get("torque_z").toString();
		data.time = object.get("time").toString();
		return data;
	}

}
