package com.surelogic.common.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.common.license.SLLicenseProduct;

public abstract class AbstractSchemaData implements SchemaData {
	protected final ClassLoader loader;
	protected final String schemaPackage;
	private final SLLicenseProduct f_product;

	protected AbstractSchemaData(String pkg, ClassLoader cl,
			SLLicenseProduct product) {
		if (pkg == null)
			throw new IllegalArgumentException(I18N.err(44, "pkg"));
		schemaPackage = pkg;
		if (cl == null)
			throw new IllegalArgumentException(I18N.err(44, "cl"));
		loader = cl;
		if (product == null)
			throw new IllegalArgumentException(I18N.err(44, "product"));
		f_product = product;
	}

	@Override
  public final SLLicenseProduct getProduct() {
		return f_product;
	}

	@Override
  public final SchemaAction getSchemaAction(final String action) {
		try {
			return (SchemaAction) newInstance(schemaPackage + "." + action);
		} catch (final InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (final IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	protected abstract Object newInstance(String qname)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException;

	protected String getSchemaResourcePath(final String resource) {
		return schemaPackage.replace(".", "/") + "/" + resource;
		// return "/" + schemaPackage.replace(".", "/") + "/" + resource;
	}

	protected abstract InputStream getResourceAsStream(String path);

	@Override
  public final int getVersion() {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				getResourceAsStream(getSchemaResourcePath("version.txt"))));
		try {
			try {
				return Integer.valueOf(reader.readLine().trim());
			} finally {
				reader.close();
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
