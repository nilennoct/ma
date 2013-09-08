package org.nilennoct.model;

import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 下午1:15
 */
public class DataTable extends Hashtable<String, String> {
	public DataTable(int size) {
		super(size);
	}

	public DataTable() {
		super();
	}
}
