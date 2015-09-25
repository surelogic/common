package com.surelogic.common.java;

import java.io.File;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import org.xml.sax.Attributes;

import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.*;

public class JavaProjectsXMLReader<P extends ISLJavaProject> extends XmlReader 
implements IXmlResultListener, PersistenceConstants {
  private final IJavaFactory<P> factory;
  private JavaProjectSet<P> projects;

  public JavaProjectsXMLReader(IJavaFactory<P> f) {
    factory = f;
  }

  public JavaProjectSet<P> readProjectsXML(File runDir) throws Exception {
	  read(new File(runDir, PersistenceConstants.PROJECTS_XML));
	  return projects;
  }
  
  @Override
  protected final String checkForRoot(String name, Attributes attributes) {
    if (PersistenceConstants.PROJECTS.equals(name)) {
      if (attributes == null) {
        return "";
      }
      String l = attributes.getValue(LOCATION);
      File loc = l == null ? null : new File(l);
      String isAuto = attributes.getValue(IS_AUTO);
      String date = attributes.getValue(DATE);
      if (date == null) {
    	// TODO update to new format
        // Get the latter half of the label
        String path = attributes.getValue(PATH);
        date = path.substring(path.indexOf(" 201")).trim();

        // Split so I can convert the dashes back to colons
        final String[] split = date.split(" ");
        if (split.length != 2) {
          throw new IllegalArgumentException();
        }
        date = split[0] + ' ' + (split[1].replace('-', ':'));
      }
      try {
        Date time = null;
        try {
        	time = SLUtility.fromStringForDir(date);
        } catch (ParseException e) {
        	time = SLUtility.fromStringDayHMS(date);
        }
        projects = factory.newProjectSet(loc, "true".equals(isAuto), time, Collections.<String, Object> emptyMap());
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
      String last = attributes.getValue(LAST_RUN);
      if (last != null) {
        projects.setPreviousPartialScan(last);
      }
      return attributes.getValue("path");
    }
    return null;
  }

  @Override
  public final Entity makeEntity(String name, Attributes a) {
    return new Entity(name, a);
  }

  @Override
  public final void start(String uid, String project) {
    // System.out.println("uid = " + uid);
  }

  protected void setupDefaultJRE(String projectName) {
	  // Nothing to do right now
  }
  
  @Override
  public void notify(Entity e) {
    if (!PROJECT.equals(e.getName())) {
      throw new IllegalStateException("Unexpected top-level entity: " + e.getName());
    }
    final String proj = e.getAttribute(NAME);
    final String location = e.getAttribute(LOCATION);
    final boolean isExported = "true".equals(e.getAttribute(IS_EXPORTED));
    final boolean hasJLO = "true".equals(e.getAttribute(HAS_JLO));
    final boolean isReal = "true".equals(e.getAttribute(REAL)) || e.getAttribute(REAL) == null;
    setupDefaultJRE(proj);
    /*
    if (proj.startsWith(Config.JRE_NAME)) {
      // TODO what if this should be JavacEclipse?
      Javac.getDefault().setPreference(IDEPreferences.DEFAULT_JRE, proj);
    }
    */

    final P p = projects.add(new Config(proj, isReal, location == null ? null : new File(location), isExported, hasJLO));
    final boolean isSource = "true".equals(e.getAttribute(Config.AS_SOURCE));
    if (isSource) {
      p.getConfig().setAsSource();
    }

    for (Entity nested : e.getReferences()) {
      final String name = nested.getName();
      if (FILE.equals(name)) {
        String path = nested.getAttributeByAliasIfPossible(PATH);
        String file = nested.getAttributeByAliasIfPossible(LOCATION);
        String qname = nested.getAttributeByAliasIfPossible(QNAME);
        String asBinary = nested.getAttribute(AS_BINARY);
        // System.out.println(proj + " has source: " + path);
        p.getConfig().addFile(new JavaSourceFile(qname, new File(file), path, "true".equals(asBinary), p.getName()));
      } else if (JAR.equals(name)) {
        String path = nested.getAttributeByAliasIfPossible(PATH);
        String orig = nested.getAttribute(ORIG_PATH);
        final boolean jarIsExported = "true".equals(nested.getAttribute(IS_EXPORTED));
        // System.out.println(proj + " has jar: " + path);
        p.getConfig().addToClassPath(new JarEntry(p.getConfig(), new File(path), new File(orig), jarIsExported));
      } else if (SRC.equals(name)) {
        String srcPath = nested.getAttribute(PATH);
        String binPath = nested.getAttribute(BIN_PATH);
        final boolean srcIsExported = "true".equals(nested.getAttribute(IS_EXPORTED));
        // System.out.println(proj + " has jar: " + path);
        p.getConfig().addToClassPath(new SrcEntry(srcIsExported, p.getConfig(), srcPath, binPath));
      } else if (PROJECT.equals(name)) {
        String pRefName = nested.getAttribute(NAME);
        // System.out.println(proj + " has ref to project " + pRefName);
        final P pRef = projects.get(pRefName);
        if (pRef != null) {
          p.getConfig().addToClassPath(pRef.getConfig());
        } else {
          SLLogger.getLogger().warning("Couldn't find project named '" + pRefName + "'");
        }
      } else if (PACKAGE.equals(name)) {
        String pkg = nested.getAttribute(NAME);
        p.getConfig().addPackage(pkg);
      } else if (OPTION.equals(name)) {
        String key = nested.getAttribute(NAME);
        String val = nested.getAttribute(VALUE);
        if (val == null) {
          // Nothing to do
        } else if ("false".equals(val) || "true".equals(val)) {
          p.getConfig().setOption(key, Boolean.parseBoolean(val));
        } else if (val.indexOf(',') >= 0) {
          p.getConfig().setOption(key, val);
        } else {
          try {
            p.getConfig().setOption(key, Integer.parseInt(val));
          } catch (NumberFormatException ex) {
            p.getConfig().setOption(key, val);
          }
        }
      } else
        throw new IllegalStateException("Unexpected entity: " + name);
    }
  }

  @Override
  public final void done() {
    // Nothing to do here?
  }
}
