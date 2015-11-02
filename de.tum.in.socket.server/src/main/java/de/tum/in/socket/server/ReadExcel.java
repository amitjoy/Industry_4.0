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
package de.tum.in.socket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Lists;

public class ReadExcel {

	/**
	 * Returns the excel column names
	 */
	private static String[] getColumnNames(final Row column) {
		final String columns[] = new String[column.getPhysicalNumberOfCells()];
		final Iterator<Cell> cellIterator = column.cellIterator();
		int i = 0;
		while (cellIterator.hasNext()) {
			final Cell cell = cellIterator.next();
			columns[i++] = cell.getStringCellValue();
		}
		return columns;
	}

	/**
	 * Returns the type of value from a cell
	 */
	private static Object getTypeValue(final Class<?> type, final Cell cell) {
		Object typedValue = null;
		final DataFormatter formatter = new DataFormatter();
		if (type == int.class) {
			typedValue = (int) cell.getNumericCellValue();
		} else if (type == double.class) {
			typedValue = cell.getNumericCellValue();
		} else if (type == boolean.class) {
			typedValue = cell.getBooleanCellValue();
		} else if (type == String.class) {
			typedValue = formatter.formatCellValue(cell);
		}
		return typedValue;
	}

	/**
	 * Loads the retrieved data from excel to the list
	 */
	private static List<RealtimeData> loadDataToList(final XSSFSheet sheet, final List<RealtimeData> data,
			final RealtimeData bluetoothData)
					throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		final Row column = sheet.getRow(0);
		final String columnNames[] = getColumnNames(column);

		final Iterator<Row> rowIterator = sheet.iterator();

		int rowOne = 0;

		while (rowIterator.hasNext()) {
			final Row row = rowIterator.next();
			final Iterator<Cell> cellIterator = row.cellIterator();

			final RealtimeData newRecord = bluetoothData.getClass().newInstance();

			if (rowOne > 0) {
				int i = 0;
				while (cellIterator.hasNext()) {
					final Cell cell = cellIterator.next();
					final String columnName = columnNames[i++];
					final Field f1 = bluetoothData.getClass().getDeclaredField(columnName.trim());
					f1.setAccessible(true);
					f1.set(newRecord, getTypeValue(f1.getType(), cell));
				}
				data.add(newRecord);
			}
			rowOne++;
		}
		return data;

	}

	/**
	 * Reads the excel
	 */
	public static List<RealtimeData> read() throws IOException {
		System.out.println("Reading dummy data from Excel.....");

		final List<RealtimeData> data = Lists.newArrayList();
		XSSFWorkbook workbook = null;

		try {

			final File file = new File("testdata.xlsx");

			final FileInputStream fileStream = new FileInputStream(file);

			// Get the workbook instance for XLS file
			workbook = new XSSFWorkbook(fileStream);

			// Get first sheet from the workbook
			final XSSFSheet sheet = workbook.getSheetAt(0);

			// load data from excel file
			final RealtimeData bluetoothData = new RealtimeData();
			loadDataToList(sheet, data, bluetoothData);

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			workbook.close();
		}
		System.out.println("Reading dummy data from Excel.....Done");
		return data;
	}
}