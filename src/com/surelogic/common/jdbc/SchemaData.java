package com.surelogic.common.jdbc;

import java.net.URL;

public interface SchemaData {

	public int getVersion();

	public URL getSchemaResource(String name);

	public SchemaAction getSchemaAction(String action);

}
