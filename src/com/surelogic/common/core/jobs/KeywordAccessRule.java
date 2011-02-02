package com.surelogic.common.core.jobs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public final class KeywordAccessRule implements ISchedulingRule {

	private final Set<String> keywords;

	KeywordAccessRule(final String[] args) {
		keywords = new HashSet<String>(Arrays.asList(args));
	}

	public static ISchedulingRule getInstance(final String... keywords) {
		return new KeywordAccessRule(keywords);
	}

	public boolean contains(final ISchedulingRule rule) {
		if (rule == this) {
			return true;
		}
		if (rule instanceof KeywordAccessRule) {
			if (keywords.equals(((KeywordAccessRule) rule).keywords)) {
				return true;
			}
		}
		return false;
	}

	public boolean isConflicting(final ISchedulingRule rule) {
		if (rule == this) {
			return true;
		}
		if (rule instanceof KeywordAccessRule) {
			final HashSet<String> intersect = new HashSet<String>(keywords);
			intersect.retainAll(((KeywordAccessRule) rule).keywords);
			return !intersect.isEmpty();
		}
		return false;
	}

}
