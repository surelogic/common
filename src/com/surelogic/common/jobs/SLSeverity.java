package com.surelogic.common.jobs;

import java.util.logging.Level;

/**
 * An IDE independent severity. This enumeration is used for IDE independent
 * status reporting.
 * 
 * @see SLStatus
 */
public enum SLSeverity {
	OK {
		@Override
		public Level toLevel() {
			return Level.FINEST;
		}
	},
	INFO {
		@Override
		public Level toLevel() {
			return Level.INFO;
		}
	},
	CANCEL {
		@Override
		public Level toLevel() {
			return Level.FINEST;
		}
	},
	WARNING {
		@Override
		public Level toLevel() {
			return Level.WARNING;
		}
	},
	ERROR {
		@Override
		public Level toLevel() {
			return Level.SEVERE;
		}
	};

	/**
	 * Converts this severity to a {@link Level} usable by the logging
	 * infrastructure.
	 * 
	 * @return a logging level.
	 */
	public abstract Level toLevel();
}
