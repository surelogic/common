package com.surelogic.common.core.preferences;

public interface IPreferenceAccessor<T> {
	T get();
	void set(T newValue);
}
