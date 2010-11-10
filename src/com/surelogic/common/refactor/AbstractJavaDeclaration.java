package com.surelogic.common.refactor;

public abstract class AbstractJavaDeclaration implements IJavaDeclaration {
	String addColons(String[] values) {
		switch (values.length) {
		case 0:
			return "";
		case 1:
			return values[0];
		default:
			final StringBuilder sb = new StringBuilder();			
			for(String v : values) {
				if (sb.length() > 0) {
					sb.append(":");
				}
				sb.append(v);
			}
			return sb.toString();
		}		
	}
	
	abstract DeclKind getKind();
}
