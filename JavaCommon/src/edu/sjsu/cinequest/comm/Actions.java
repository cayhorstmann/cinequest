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

	public static Action ifThenElse(final Action first, final Action second, final Action third) {
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
						else third.start(in, cb);
					}
				});
			}
		};
	}

	public static Action ifThen(final Action first, final Action second) {
		return ifThenElse(first, second, nothing());
	}

	public static Action nothing() {
		return new Action() {
			public void start(Object in, Callback cb) {
				cb.invoke(in);
			}
		};
	}
}
