grammar ColumnAnnotation;

@parser::header {
package com.surelogic.common.adhoc.model.parser;
import com.surelogic.common.Justification;
import com.surelogic.common.adhoc.model.ColumnAnnotation;
}
@lexer::header{
package com.surelogic.common.adhoc.model.parser;
}

@parser::members {
private final ColumnAnnotation f_column = new ColumnAnnotation();

@Override
protected void mismatch(IntStream input, int ttype, BitSet follow)
		throws RecognitionException {
	throw new MismatchedTokenException(ttype, input);
}

@Override
public Object recoverFromMismatchedSet(IntStream input,
		RecognitionException e, BitSet follow) throws RecognitionException {
	throw e;
}
}
@lexer::members {
@Override
public void reportError(RecognitionException e) {
  Thrower.sneakyThrow(e);
}

/**
 * See "Puzzle 43: Exceptionally Unsafe" from Bloch Gafter, <i>Java Puzzlers</i>. Addison Wesley 2005.
 */
static class Thrower {
	private static Throwable t;
	private Thrower() throws Throwable {
		throw t;
	}
	public static synchronized void sneakyThrow(Throwable t) {
		Thrower.t = t;
		try {
			Thrower.class.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} finally {
			Thrower.t = null; // Avoid memory leak
		}
	}
}
}

@rulecatch {
catch (RecognitionException e) {
	throw e;
}
}

columnAnnotation returns [ColumnAnnotation result]
	: (annotationPart)* { $result = f_column; }
	;

annotationPart
	: treeTableSpec
	| '(' (hideSpec | iconSpec | justSpec | countSpec | sumSpec | maxSpec) ')'
	;

treeTableSpec
	: '|' { f_column.setIsLastTreeColumn(true); }
	| ']' { f_column.setLastInitiallyVisible(true); }
	;

hideSpec 
	: 'hide' { f_column.setIsHidden(true); }
	;

iconSpec
	: name=QUOTED_STRING { f_column.setIconName($name.text); } dotdotdotPart?
	| 'icon' { f_column.setDefinesAnIconForAnotherColumn(true); }
	;
	
dotdotdotPart
	: '...' { f_column.setShowIconWhenEmpty(false); }
	;

justSpec
	: ('r' | 'right') { f_column.setJusification(Justification.RIGHT); }
	| ('c' | 'center')  { f_column.setJusification(Justification.CENTER); }
	| ('l' | 'left') { f_column.setJusification(Justification.LEFT); }
	;

countSpec
	: 'count' { f_column.setCountPartialRows(true); } distinctPart? replaceValuePart? onPart? suffixPart?
	;

sumSpec : 'sum' { f_column.setSumPartialRows(true); } onPart? suffixPart?
	;
	
maxSpec : 'max' { f_column.setMaxPartialRows(true); } onPart? suffixPart?
	;

distinctPart
	: 'distinct' { f_column.setCountDistinct(true); }
	;

onPart
	: 'on' i=INT { f_column.addToOnSet($i.text); } (',' i=INT { f_column.addToOnSet($i.text); } )*
	;
	
replaceValuePart
	: 'replace-value-with' replace=QUOTED_STRING { f_column.setCountReplaceValueWith($replace.text); }
	;

suffixPart
	: suffix=QUOTED_STRING { f_column.setSuffix($suffix.text); }
	;

INT
	: ('0'..'9')+
	;

QUOTED_STRING
	: '\'' ( ~'\'' )* '\''
	;

WS
	: (' '|'\t'|'\n'|'\r')+ { skip(); }
	;

