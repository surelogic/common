package com.surelogic.common.adhoc.model;

import java.util.logging.Level;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.surelogic.common.adhoc.model.parser.ColumnAnnotationLexer;
import com.surelogic.common.adhoc.model.parser.ColumnAnnotationParser;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

public final class ColumnAnnotationParserUtility {

	public static ColumnAnnotation parse(String annotation) {
		ColumnAnnotation result = null;
		ANTLRStringStream input = new ANTLRStringStream(annotation);
		ColumnAnnotationLexer lexer = new ColumnAnnotationLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ColumnAnnotationParser parser = new ColumnAnnotationParser(tokens);
		try {
			result = parser.columnAnnotation();
		} catch (RecognitionException e) {
			SLLogger.getLogger().log(Level.WARNING,
					I18N.err(129, annotation, e.charPositionInLine));
		}
		if (result == null) {
			result = new ColumnAnnotation();
			result.setIsValid(false);
		}
		return result;
	}

	private ColumnAnnotationParserUtility() {
		// no instances
	}
}
