package com.surelogic.server.jdbc.schema;

import java.io.InputStream;
import java.net.URL;

import com.surelogic.common.jdbc.AbstractSchemaData;
import com.surelogic.common.license.SLLicenseProduct;

public class ServicesSchemaData extends AbstractSchemaData {
  public ServicesSchemaData() {
    super("com.surelogic.server.jdbc.schema", Thread.currentThread().getContextClassLoader(), SLLicenseProduct.EXEMPT);
  }

  protected Object newInstance(String qname) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    return loader.loadClass(qname).newInstance();
  }

  public URL getSchemaResource(final String name) {
    return loader.getResource(getSchemaResourcePath(name));
  }

  protected InputStream getResourceAsStream(String path) {
    return loader.getResourceAsStream(path);
  }
}
