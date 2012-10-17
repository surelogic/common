package com.surelogic.common.refactor;

import com.surelogic.common.ref.DeclUtil;
import com.surelogic.common.ref.IDecl;

public class ScopedPromiseUtil {
	/**
	 * Should include any necessary 'in' clauses
	 */
	public static String getForSyntax(IDecl decl) {		
		StringBuilder sb = new StringBuilder();
		switch (decl.getKind()) {
		case CLASS:
		case ENUM:
		case INTERFACE:
			String name = DeclUtil.getTypeNameOrEmpty(decl);
			if (name.indexOf('.') >= 0) {
				throw new UnsupportedOperationException();	
			}
			String pkg = DeclUtil.getPackageNameOrNull(decl);
			if (pkg == null) {
				sb.append(decl.getName());
			} else {
				sb.append(decl.getName()).append(" in ").append(pkg);
			}
			break;
		case CONSTRUCTOR:
			sb.append("new(");
			addParameters(decl, sb);
			addTypeAndPackage(decl, sb);
			break;
		case METHOD:			
			sb.append(decl.getName()).append('(');
			addParameters(decl, sb);
			addTypeAndPackage(decl, sb);
			break;
		case FIELD:
			sb.append(decl.getTypeOf().getFullyQualified()).append(' ').append(decl.getName());
			addTypeAndPackage(decl, sb);
			break;
		default:
			throw new UnsupportedOperationException();		
		}
		return sb.toString();
	}
	
	private static void addTypeAndPackage(IDecl decl, StringBuilder sb) {
		sb.append(" in ");
		final String name = DeclUtil.getTypeNameOrNull(decl);
		if (name == null) {
			throw new UnsupportedOperationException();	
		}
		String pkg = DeclUtil.getPackageNameOrNull(decl);
		if (pkg == null) {
			sb.append(name);
		} else {
			sb.append(name).append(" in ").append(pkg);
		}
	}

	private static void addParameters(IDecl decl, StringBuilder sb) {
		boolean first = true;
		for(IDecl param : decl.getParameters()) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append(param.getTypeOf().getFullyQualified());
		}
		sb.append(')');
	}
}
