package com.surelogic.common.eclipse.core.preferences;

public interface IPreferenceAccessor<T> {
	T get();
	void set(T newValue);
}
