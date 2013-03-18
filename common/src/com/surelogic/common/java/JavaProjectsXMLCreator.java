package com.surelogic.common.java;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.surelogic.common.SLUtility;
import com.surelogic.common.java.IClassPathEntry;
import com.surelogic.common.java.JavaSourceFile;
import com.surelogic.common.java.PersistenceConstants;
import com.surelogic.common.xml.XmlCreator;

public class JavaProjectsXMLCreator extends XmlCreator implements PersistenceConstants {

  public JavaProjectsXMLCreator(OutputStream out) throws IOException {
    super(out);
  }

  public void write(JavaProjectSet<?> projs) {
    try {
      int i = 0;
      b.start(PROJECTS);
      b.addAttribute("path", projs.getRun());
      if (projs.getLocation() != null) {
        b.addAttribute(LOCATION, projs.getLocation().getAbsolutePath());
      }
      b.addAttribute(IS_AUTO, projs.isAutoBuild());
      b.addAttribute(LAST_RUN, projs.getPreviousPartialScan());
      b.addAttribute(DATE, SLUtility.toStringForDir(projs.getDate()));
      for (ISLJavaProject p : projs) {
        final Builder pb = b.nest(PROJECT);
        pb.addAttribute("id", i);
        pb.addAttribute(NAME, p.getName());
        if (p.getConfig().getLocation() != null) {
          pb.addAttribute(LOCATION, p.getConfig().getLocation().getAbsolutePath());
        }
        pb.addAttribute(IS_EXPORTED, p.getConfig().isExported());
        if (p.getConfig().containsJavaLangObject()) {
          pb.addAttribute(HAS_JLO, p.getConfig().containsJavaLangObject());
        }
        p.getConfig().outputOptionsToXML(pb);

        for (IClassPathEntry cpe : p.getConfig().getClassPath()) {
          cpe.outputToXML(pb);
        }
        for (JavaSourceFile f : p.getConfig().getFiles()) {
          f.outputToXML(pb);
        }
        for (String pkg : p.getConfig().getPackages()) {
          final Builder pkb = b.nest(PACKAGE);
          pkb.addAttribute(NAME, pkg);
          pkb.end();
        }
        for (Map.Entry<String, Object> option : p.getConfig().getOptions()) {
          final Builder ob = b.nest(OPTION);
          ob.addAttribute(NAME, option.getKey());
          ob.addAttribute(VALUE, option.getValue().toString());
          ob.end();
        }
        pb.end();
        i++;
      }
      b.end();
    } finally {
      flushBuffer();
    }
  }
}
