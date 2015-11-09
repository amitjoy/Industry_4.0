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

import org.osgi.service.component.annotations.Component;

import de.tum.in.realtime.data.operation.api.DataOperation;
import de.tum.in.realtime.data.operation.api.RealtimeData;

/**
 * Service Interface {@link DataOperation} Implementation
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(name = "de.tum.in.realtime.data.operation")
public final class DataOperationImpl implements DataOperation {

	/** {@inheritDoc}} */
	@Override
	public List<RealtimeData> retrieveAll() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

}
