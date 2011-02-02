package com.surelogic.common.eclipse.preferences;

public interface IPreferenceAccessor<T> {
	T get();
	void set(T newValue);
}
