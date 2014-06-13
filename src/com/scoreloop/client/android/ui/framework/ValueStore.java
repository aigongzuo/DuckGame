/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.ui.framework;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValueStore {

	public static interface Continuation {
		void withValue(Object value);
	}

	public static interface Observer {
		void onValueChanged(ValueStore valueStore, String key, Object oldValue, Object newValue);

		void onValueSetDirty(ValueStore valueStore, String key);
	}

	private static interface ObserverContinuation {
		void withObsever(Observer observer);
	}

	public static enum RetrievalMode {
		NOT_DIRTY, NOT_OLDER_THAN
	}

	public static interface ValueSource {
		boolean isRetrieving();

		void retrieve(ValueStore valueStore);

		void supportedKeys(Set<String> keys);
	}

	public static interface ValueSourceFactory {
		ValueSource getValueSourceForKeyInStore(String key, ValueStore valueStore);
	}

	public static String concatenateKeys(final String... keys) {
		if (keys.length == 0) {
			return null;
		} else if (keys.length == 1) {
			return keys[0];
		} else {
			final StringBuilder builder = new StringBuilder();
			builder.append(keys[0]);
			for (int i = 1; i < keys.length; ++i) {
				builder.append('/');
				builder.append(keys[i]);
			}
			return builder.toString();
		}
	}

	private static boolean isPlainKey(final String key) {
		return key.indexOf("/") == -1;
	}

	private static String[] splitKeyComponents(final String key) {
		return key.split("/", 2);
	}

	private final Map<String, Date>						_dates	= new HashMap<String, Date>();
	private ValueSourceFactory							_factory;
	private Map<String, List<WeakReference<Observer>>>	_observerMap;
	private final Map<String, Object>					_values	= new HashMap<String, Object>();

	private List<ValueSource>							_valueSources;

	public void addObserver(final String key, final Observer observer) {
		if (isPlainKey(key)) {
			List<WeakReference<Observer>> observers = getObserverMap().get(key);
			if (observers == null) {
				observers = new ArrayList<WeakReference<Observer>>();
				_observerMap.put(key, observers);
			}
			observers.add(new WeakReference<Observer>(observer));
		} else {
			final String[] components = splitKeyComponents(key);
			final String rootKey = components[0];
			ValueStore childStore = getValue(rootKey);
			if (childStore == null) {
				childStore = new ValueStore();
				putValue(rootKey, childStore);
			}
			childStore.addObserver(components[1], observer);
		}
	}

	public void addValueSources(final ValueSource... sources) {
		Collections.addAll(getValueSources(), sources);
	}

	void copyFromOtherForKeys(final ValueStore otherValues, final Set<String> keys) {
		for (final String key : keys) {
			if (isPlainKey(key)) {
				final Object otherValue = otherValues.getValue(key);
				putValue(key, otherValue);
			} else {
				final String[] components = splitKeyComponents(key);
				final String rootKey = components[0];
				ValueStore thisChild = getValue(rootKey);
				if (thisChild == null) {
					thisChild = new ValueStore();
					putValue(rootKey, thisChild);
				}
				final ValueStore otherChild = otherValues.getValue(rootKey);
				thisChild.copyFromOtherForKeys(otherChild, Collections.singleton(components[1]));
			}
		}
	}

	private void forAllObservers(final String key, final ObserverContinuation continuation) {
		final List<WeakReference<Observer>> observers = getObserverMap().get(key);
		if (observers == null) {
			return;
		}
		final List<WeakReference<Observer>> copiedObservers = new ArrayList<WeakReference<Observer>>(observers);
		for (final WeakReference<Observer> weakObserver : copiedObservers) {
			final Observer observer = weakObserver.get();
			if (observer != null) {
				continuation.withObsever(observer);
			} else {
				observers.remove(weakObserver);
			}
		}
	}

	private Date getDate(final String key) {
		return _dates.get(key);
	}

	private Map<String, List<WeakReference<Observer>>> getObserverMap() {
		if (_observerMap == null) {
			_observerMap = new HashMap<String, List<WeakReference<Observer>>>();
		}
		return _observerMap;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(final String key) {
		return (T) getValue(key, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(final String key, final T defaultIfNotFound) {
		if (_values.containsKey(key)) {
			return (T) _values.get(key);
		}
		return defaultIfNotFound;
	}

	private ValueSource getValueSource(final String key) {
		final Set<String> keys = new HashSet<String>();
		for (final ValueSource source : getValueSources()) {
			source.supportedKeys(keys);
			if (keys.contains(key)) {
				return source;
			}
		}

		// no source found for key, so create one if possible
		if (_factory == null) {
			return null;
		}
		final ValueSource valueSource = _factory.getValueSourceForKeyInStore(key, this);
		if (valueSource != null) {
			addValueSources(valueSource);
		}
		return valueSource;
	}

	private List<ValueSource> getValueSources() {
		if (_valueSources == null) {
			_valueSources = new ArrayList<ValueSource>();
		}
		return _valueSources;
	}

	private void invokeChangedObservers(final String key, final Object oldValue, final Object newValue) {
		forAllObservers(key, new ObserverContinuation() {
			public void withObsever(final Observer observer) {
				observer.onValueChanged(ValueStore.this, key, oldValue, newValue);
			}
		});
	}

	private void invokeSetDirtyObservers(final String key) {
		forAllObservers(key, new ObserverContinuation() {
			public void withObsever(final Observer observer) {
				observer.onValueSetDirty(ValueStore.this, key);
			}
		});
	}

	private boolean isClean(final String key, final RetrievalMode mode, final Object argument) {
		final Date date = getDate(key);
		if (mode == RetrievalMode.NOT_DIRTY) {
			return date != null;
		} else if (mode == RetrievalMode.NOT_OLDER_THAN) {
			if (date == null) {
				return false;
			}
			final Date reference = new Date();
			reference.setTime(reference.getTime() - (Long) argument);
			return !date.before(reference);
		}
		return true;
	}

	public boolean isDirty(final String key) {
		return _dates.get(key) == null;
	}

	public ValueStore putValue(final String key, final Object newValue) {
		final Object oldValue = _values.get(key);
		_values.put(key, newValue);
		_dates.put(key, new Date());
		if (oldValue != newValue) {
			invokeChangedObservers(key, oldValue, newValue);
		}
		return this;
	}

	public void removeObserver(final String key, final Observer anObserver) {
		if (isPlainKey(key)) {
			final List<WeakReference<Observer>> observers = getObserverMap().get(key);
			if (observers != null) {
				final List<WeakReference<Observer>> copiedObservers = new ArrayList<WeakReference<Observer>>(observers);
				for (final WeakReference<Observer> weakObserver : copiedObservers) {
					final Observer observer = weakObserver.get();
					if ((observer == null) || (observer == anObserver)) {
						observers.remove(weakObserver);
					}
				}
			}
		} else {
			final String[] components = splitKeyComponents(key);
			final ValueStore childStore = getValue(components[0]);
			if (childStore != null) {
				childStore.removeObserver(components[1], anObserver);
			}
		}
	}

	public boolean retrieveValue(final String key, final RetrievalMode mode, final Object argument) {
		if (isClean(key, mode, argument)) {
			return true;
		}

		final ValueSource source = getValueSource(key);
		if ((source != null) && !source.isRetrieving()) {
			source.retrieve(this);
		}
		return false;
	}

	public Observer retrieveValue(final String key, final RetrievalMode mode, final Object argument, final Continuation continuation) {
		if (isClean(key, mode, argument)) {
			continuation.withValue(getValue(key));
			return null;
		} else {
			final ValueSource source = getValueSource(key);
			if (source == null) {
				continuation.withValue(null);
				return null;
			} else {
				final Observer observer = new Observer() {
					public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
						removeObserver(key, this);
						continuation.withValue(newValue);
					}

					public void onValueSetDirty(final ValueStore valueStore, final String key) {
					}
				};
				addObserver(key, observer);
				if (!source.isRetrieving()) {
					source.retrieve(this);
				}
				return observer;
			}
		}
	}

	void runObserverForKeys(final ValueStore oldValues, final Set<String> keys, final Observer observer) {
		for (final String key : keys) {
			if (isPlainKey(key)) {
				observer.onValueSetDirty(this, key);

				final Object oldValue = oldValues != null ? oldValues.getValue(key) : null;
				final Object newValue = getValue(key);
				if (oldValue != newValue) {
					observer.onValueChanged(this, key, oldValue, newValue);
				}
			} else {
				final String[] components = splitKeyComponents(key);
				final String rootKey = components[0];
				final ValueStore oldChild = oldValues != null ? oldValues.<ValueStore> getValue(rootKey) : null;
				final ValueStore newChild = getValue(rootKey);
				newChild.runObserverForKeys(oldChild, Collections.singleton(components[1]), observer);
			}
		}
	}

	public void setAllDirty() {
		_dates.clear();
		for (final String key : _values.keySet()) {
			invokeSetDirtyObservers(key);
		}
	}

	public void setDirty(final String key) {
		_dates.put(key, null);
		invokeSetDirtyObservers(key);
	}

	public void setValueSourceFactroy(final ValueSourceFactory factory) {
		_factory = factory;
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(super.toString());
		buffer.append(" [");
		boolean isFirst = true;
		for (final String key : _values.keySet()) {
			if (!isFirst) {
				buffer.append(", ");
			}
			buffer.append(key);
			buffer.append("=");
			buffer.append(_values.get(key));
			if (_dates.get(key) == null) {
				buffer.append("(*)");
			}
			isFirst = false;
		}
		buffer.append("]");
		return buffer.toString();
	}
}
