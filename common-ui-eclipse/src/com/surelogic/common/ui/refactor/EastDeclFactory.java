package com.surelogic.common.ui.refactor;

import org.eclipse.jdt.core.dom.*;

import com.surelogic.common.ref.*;
import com.surelogic.common.ref.Decl.*;

/**
 * Only creates enough of the IDecl for specification purposes
 * 
 * @author edwin
 */
public class EastDeclFactory {
	public static IDeclType createDeclType(AbstractTypeDeclaration t) {
		return (IDeclType) makeBuilder(t).build();
	}

	public static IDeclType createDeclType(AnonymousClassDeclaration node) {
		return (IDeclType) makeBuilder(node).build();
	}
	
	public static IDeclField createDeclField(VariableDeclarationFragment frag) {
		return (IDeclField) makeBuilder(frag).build();
	}

	public static IDeclParameter createDeclParameter(SingleVariableDeclaration p, int i) {
		return (IDeclParameter) makeBuilder(p).build();
	}

	public static IDeclFunction createDeclFunction(MethodDeclaration node) {
		return (IDeclFunction) makeBuilder(node).build();
	}	
	
	private static DeclBuilder makeBuilder(ASTNode n) {
		if (n == null) {
			return null;
		}
		DeclBuilder parent = makeBuilder(n.getParent());
		DeclBuilder me;
		int position = 0;
		switch(n.getNodeType()) {
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
			me = makeAnnotationTypeBuilder((AnnotationTypeDeclaration) n);
			break;
		case ASTNode.ANONYMOUS_CLASS_DECLARATION:
			me = makeAnonTypeBuilder((AnonymousClassDeclaration) n);
			break;
		case ASTNode.ENUM_DECLARATION:
			me = makeEnumBuilder((EnumDeclaration) n);
			break;
		case ASTNode.TYPE_DECLARATION:
			me = makeTypeBuilder((TypeDeclaration) n);
			break;
		case ASTNode.TYPE_PARAMETER:		
			TypeDeclaration type = (TypeDeclaration) n.getParent();
			for (Object o : type.typeParameters()) {
				if (o == n) {
					break;
				}
				position++;
			}
			me = parent.getTypeParameterBuilderAt(position);
			break;
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			me = makeFieldBuilder(parent, (VariableDeclarationFragment) n);
			break;
		case ASTNode.METHOD_DECLARATION:
			me = makeMethodBuilder((MethodDeclaration) n);
			break;
		case ASTNode.PACKAGE_DECLARATION:
			me = makePackageBuilder((PackageDeclaration) n);
			break;
		case ASTNode.SINGLE_VARIABLE_DECLARATION:
			if (n.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration m = (MethodDeclaration) n.getParent();
				for (Object o : m.parameters()) {
					if (o == n) {
						break;
					}
					position++;
				}
				me = parent.getParameterBuilderAt(position);
				break;
			}
		default:
			me = null;
		}
		if (me == null) {
			return parent;
		}
		if (parent != null) {
			me.setParent(parent);
		} else if (n instanceof AbstractTypeDeclaration) { 
			// me with no parent
			CompilationUnit cu = (CompilationUnit) n.getParent();
			parent = makePackageBuilder(cu.getPackage());
			me.setParent(parent);
		}
		return me;
	}

	private static DeclBuilder makeFieldBuilder(DeclBuilder parent, VariableDeclarationFragment n) {
		if (n.getParent().getNodeType() != ASTNode.FIELD_DECLARATION) {
			return null;
		}
		FieldBuilder b = new FieldBuilder(n.getName().getIdentifier());
		ITypeBinding type = n.resolveBinding().getType();
		b.setTypeOf(new TypeRef(type.getQualifiedName(), type.getName()));
		return b;
	}

	private static DeclBuilder makeMethodBuilder(MethodDeclaration n) {
		if (n.isConstructor()) {
			ConstructorBuilder c = new ConstructorBuilder();
			int i=0;
			for(Object o : n.parameters()) {
				SingleVariableDeclaration p = (SingleVariableDeclaration) o;
				c.addParameter(makeParameterBuilder(p, i));
				i++;
			}
			return c;
		} else {
			MethodBuilder b = new MethodBuilder(n.getName().getIdentifier());
			int i=0;
			for(Object o : n.parameters()) {
				SingleVariableDeclaration p = (SingleVariableDeclaration) o;
				b.addParameter(makeParameterBuilder(p, i));
				i++;
			}
			return b;
		}
	}

	private static ParameterBuilder makeParameterBuilder(SingleVariableDeclaration p, int i) {
		ParameterBuilder b = new ParameterBuilder(i, p.getName().getIdentifier());
		final ITypeBinding type = p.resolveBinding().getType();		
		final String qname = type.getQualifiedName();			
		final String name = computeRelativeName(type);
		b.setTypeOf(new TypeRef(qname, name));
		return b;
	}

	private static String computeRelativeName(ITypeBinding type) {
		final ITypeBinding enclosingType = type.getDeclaringClass();
		final String name;
		
		// Hacks needed to handle nested and generic types		
		if (enclosingType != null) {
			name = computeRelativeNameNoBounds(enclosingType.getErasure())+'.'+type.getName();
		} else if (type.isArray()) {
			return computeRelativeName(type.getComponentType())+"[]";
		} else {
			name = type.getName();
		}
		/*
		final String pkgName = type.getPackage() == null ? null : type.getPackage().getName();
		if (pkgName != null && qname.startsWith(pkgName)) {
			b.setTypeOf(new TypeRef(qname, qname.substring(pkgName.length()+1)));
		} else {
			b.setTypeOf(new TypeRef(qname, type.getName()));
		}
		*/
		return name;
	}
	
	/**
	 * No bounds -- just the type name;
	 */
	private static String computeRelativeNameNoBounds(ITypeBinding type) {
		final ITypeBinding enclosingType = type.getDeclaringClass();
		if (enclosingType == null) {
			return type.getName();
		}
		return computeRelativeName(enclosingType)+'.'+type.getElementType();				
	}
	
	private static TypeParameterBuilder makeTypeParameterBuilder(TypeParameter n, int i) {
		TypeParameterBuilder b = new TypeParameterBuilder(i, n.getName().getIdentifier());
		return b;
	}
	
	private static DeclBuilder makeTypeBuilder(TypeDeclaration n) {
		final String name = n.getName().getIdentifier();
		if (n.isInterface()) {
			InterfaceBuilder b = new InterfaceBuilder(name);
			int i=0;
			for(Object o : n.typeParameters()) {
				TypeParameter p = (TypeParameter) o;
				b.addTypeParameter(makeTypeParameterBuilder(p, i));
				i++;
			}
			return b;
		}
		ClassBuilder b =  new ClassBuilder(name);		
		int i=0;
		for(Object o : n.typeParameters()) {
			TypeParameter p = (TypeParameter) o;
			b.addTypeParameter(makeTypeParameterBuilder(p, i));
			i++;
		}
		return b;
	}
	
	private static DeclBuilder makeAnnotationTypeBuilder(AnnotationTypeDeclaration n) {
		return new AnnotationBuilder(n.getName().getIdentifier());
	}

	private static DeclBuilder makeAnonTypeBuilder(AnonymousClassDeclaration n) {		
		return new ClassBuilder(null).setVisibility(IDecl.Visibility.ANONYMOUS);
	}

	private static DeclBuilder makeEnumBuilder(EnumDeclaration n) {
		return new EnumBuilder(n.getName().getIdentifier());
	}

	private static DeclBuilder makePackageBuilder(PackageDeclaration n) {
		return new PackageBuilder(n.getName().getFullyQualifiedName());
	}
}
