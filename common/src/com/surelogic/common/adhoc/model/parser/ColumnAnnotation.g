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
	| '(' (hideSpec | iconSpec | justSpec | affixSpec | numSpec | blankIfSpec | changeIfSpec | countSpec | sumSpec | maxSpec | containsSpec) ')'
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
	
affixSpec
	: 'prefix' value=QUOTED_STRING { f_column.setPrefix($value.text); }
	| 'prefix-only-if-nonempty' value=QUOTED_STRING { f_column.setPrefix($value.text); f_column.setPrefixOnlyIfNonempty(true); }
	| 'suffix' value=QUOTED_STRING { f_column.setSuffix($value.text); }
	| 'suffix-only-if-nonempty' value=QUOTED_STRING { f_column.setSuffix($value.text); f_column.setSuffixOnlyIfNonempty(true); }
	;
	
numSpec
	: 'add-commas' { f_column.setAddCommas(true); }
	| 'human-readable-duration' { f_column.setHumanReadableDuration(true); } unitPart?
	;
	
unitPart
	: 'unit' value=QUOTED_STRING { f_column.setHumanReadableDurationUnit($value.text); }
	;
	
blankIfSpec
	: 'blank-if' value=QUOTED_STRING { f_column.setBlankIf($value.text); }
	;

changeIfSpec
	: 'change-if' fValue=QUOTED_STRING 'to' tValue=QUOTED_STRING { f_column.setChangeIf($fValue.text, $tValue.text); }
	;

countSpec
	: 'count' { f_column.setCountPartialRows(true); } distinctPart?  nonemptyPart? replaceValuePart? onPart? prefixPart? suffixPart?
	;

sumSpec
    : 'sum' { f_column.setSumPartialRows(true); } onPart? prefixPart? suffixPart?
	;
	
maxSpec
    : 'max' { f_column.setMaxPartialRows(true); } onPart? prefixPart? suffixPart?
	;
	
containsSpec
    : 'contains' { f_column.setContainsPartialRows(true); } showPart onPart? prefixPart? suffixPart?
	;
	
showPart
	: cValue=QUOTED_STRING 'show' sValue=QUOTED_STRING { f_column.setContainsValue($cValue.text);  f_column.setShowValue($sValue.text);}
	;


distinctPart
	: 'distinct' { f_column.setCountDistinct(true); }
	;

nonemptyPart
	: 'nonempty' { f_column.setCountNonempty(true); }
	;

onPart
	: 'on' i=INT { f_column.addToOnSet($i.text); } (',' i=INT { f_column.addToOnSet($i.text); } )*
	;
	
replaceValuePart
	: 'replace-value-with' replace=QUOTED_STRING { f_column.setCountReplaceValueWith($replace.text); }
	;

prefixPart
	: 'prefix' value=QUOTED_STRING { f_column.setAggregatePrefix($value.text); }
	;

suffixPart
	: 'suffix' value=QUOTED_STRING { f_column.setAggregateSuffix($value.text); }
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

