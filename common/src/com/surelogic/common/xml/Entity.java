package com.surelogic.common.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.surelogic.common.ref.*;

public class Entity {
  final String name;
  protected final Map<String, String> attributes;
  final String id;
  List<Entity> refs = Collections.emptyList();
  StringBuilder chars;
  String contents;

  public final void addToCData(char[] buf, int offset, int len) {
    if (chars == null) {
      chars = new StringBuilder(len);
    }
    chars.append(buf, offset, len);
  }

  public String getCData() {
    if (contents == null) {
      if (chars == null) {
        contents = null;
      } else {
        contents = chars.toString();
      }
    }
    return contents;
  }

  public static String getValue(Attributes a, String name) {
    for (int i = 0; i < a.getLength(); i++) {
      final String aName = a.getQName(i);
      if (name.equals(aName)) {
        final String aValue = a.getValue(i);
        return aValue;
      }
    }
    return null;
  }

  public Entity(String name, Attributes a) {
    this.name = name;
    if (a != null) {
      attributes = new HashMap<>(a.getLength(), 1.0f);
      for (int i = 0; i < a.getLength(); i++) {
        final String aName = a.getQName(i);
        final String aValue = a.getValue(i);
        attributes.put(aName, aValue);
      }
    } else {
      attributes = new HashMap<>(4, 1.0f);
    }
    id = attributes.get(XmlReader.ID_ATTR);
  }

  public Entity(String name, Map<String, String> a) {
    this.name = name;
    if (a != null) {
      attributes = new HashMap<>(a.size(), 1.0f);
      attributes.putAll(a);
    } else {
      attributes = new HashMap<>(4, 1.0f);
    }
    id = attributes.get(XmlReader.ID_ATTR);
  }

  @Override
  public final String toString() {
    return name;
  }

  public final String getName() {
    return name;
  }

  public final String getEntityName() {
    return getName();
  }

  public String getId() {
    return id;
  }

  public void addRef(Entity e) {
    if (refs.isEmpty()) {
      refs = new ArrayList<>(1);
    }
    refs.add(e);
  }

  public final int numRefs() {
    return refs.size();
  }

  public final Iterable<Entity> getReferences() {
    return refs;
  }

  public final Map<String, String> getAttributes() {
    return attributes;
  }

  public final String getAttribute(String a) {
    return attributes.get(a);
  }

  /**
   * Tries to cache the value before returning it&mdash;to alias duplicate
   * strings.
   * 
   * @param value
   *          a string
   * @return a string, aliased to avoid duplicate strings.
   * 
   * @see DeclUtil#aliasIfPossible(String)
   */
  public final String getAttributeByAliasIfPossible(String value) {
    return DeclUtil.aliasIfPossible(attributes.get(value));
  }

  public IJavaRef parsePersistedRef(String encode) {
    return JavaRef.parseEncodedForPersistence(encode);
  }

  public boolean hasRefs() {
    return !refs.isEmpty();
  }
}
