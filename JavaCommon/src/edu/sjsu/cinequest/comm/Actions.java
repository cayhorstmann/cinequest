package edu.sjsu.cinequest.comm;

/**
 * Utility methods for combining actions
 */
public class Actions {
	/**
	 * Combines two actions
	 * 
	 * @param first
	 *            an action
	 * @param second
	 *            another action
	 * @return an action that carries out first, and, if it is successful,
	 *         second. The result of first becomes the input of second. If
	 *         either action fails, the combined action fails.
	 */
	
	public static Action andThen(final Action first, final Action second) {
		return new Action() {
			public void start(Object in, final Callback cb) {
				first.start(in, new Callback() {
					public void progress(Object value) {
						cb.progress(value);
					}

					public void failure(Throwable t) {
						cb.failure(t);
					}

					public void invoke(Object result) {
						second.start(result, cb);						
					}});
			}
		};
	}
	
	// TODO: Eliminate?
	// No varargs in Java 1.3 :-(
	public static Action andThen(final Action[] actions) {
		return new Action() {
			private int current = 0;
			private Callback cb1;
			public void start(Object in, final Callback cb) {
				if (actions.length == 0) { cb.invoke(null); return; }
				cb1 = new Callback() {
					public void progress(Object value) {
						cb.progress(value);
					}

					public void failure(Throwable t) {
						cb.failure(t);
					}

					public void invoke(Object result) {
						current++;
						if (current < actions.length)
							actions[current].start(result, cb1);
						else 
							cb.invoke(result);
					}};
				actions[0].start(in, cb1);
			}
		};
	}

	public static abstract class Step implements Action {
		public abstract Object invoke(Object in);

		public void start(Object in, Callback cb) {
			try {
				cb.invoke(invoke(in));
			} catch (Throwable t) {
				cb.failure(t);
			}
		}
	}

	// TODO: Should it be ifThenElse?
	public static Action ifThen(final Action first, final Action second) {
		return new Action() {
			public void start(final Object in, final Callback cb) {
				first.start(in, new Callback() {
					public void progress(Object value) {
						cb.progress(value);
					}

					public void failure(Throwable t) {
						cb.failure(t);
					}

					public void invoke(Object result) {
						if (result == Boolean.TRUE) second.start(in, cb);
						else cb.invoke(in);
					}
				});
			}
		};
	}

	
	
	// TODO: Eliminate?
	public static Action orElse(final Action first, final Action second) {
		return new Action() {
			public void start(final Object in, final Callback cb) {
				first.start(in, new Callback() {
					public void progress(Object value) {
						cb.progress(value);
					}

					public void failure(Throwable t) {
						second.start(t, cb);
					}

					public void invoke(Object result) {
						cb.invoke(result);
					}
				});
			}
		};
	}

	// TODO: Eliminate?
	public static Action withResult(final Object result) {
		return new Action() {
			public void start(Object in, Callback cb) {
				cb.invoke(result);
			}
		};
	}

	public static Action nothing() {
		return new Action() {
			public void start(Object in, Callback cb) {
				cb.invoke(in);
			}
		};
	}
}
