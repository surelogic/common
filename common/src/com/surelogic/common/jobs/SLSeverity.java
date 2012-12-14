package com.surelogic.common.jobs;

import java.util.logging.Level;

import com.surelogic.Immutable;

/**
 * An IDE independent severity. This enumeration is used for IDE independent
 * status reporting.
 * 
 * @see SLStatus
 */
@Immutable
public enum SLSeverity {
  OK {
    @Override
    public Level toLevel() {
      return Level.FINEST;
    }

    @Override
    public boolean isProblem() {
      return false;
    }
  },
  INFO {
    @Override
    public Level toLevel() {
      return Level.INFO;
    }

    @Override
    public boolean isProblem() {
      return false;
    }
  },
  CANCEL {
    @Override
    public Level toLevel() {
      return Level.FINEST;
    }

    @Override
    public boolean isProblem() {
      return false;
    }
  },
  WARNING {
    @Override
    public Level toLevel() {
      return Level.WARNING;
    }

    @Override
    public boolean isProblem() {
      return true;
    }
  },
  ERROR {
    @Override
    public Level toLevel() {
      return Level.SEVERE;
    }

    @Override
    public boolean isProblem() {
      return true;
    }
  };

  /**
   * Converts this severity to a {@link Level} usable by the logging
   * infrastructure.
   * 
   * @return a logging level.
   */
  public abstract Level toLevel();

  /**
   * Indicates that this severity indicates some kind of problem.
   * 
   * @return {@code true} if the severity indicates a problem, {@code false}
   *         otherwise.
   */
  public abstract boolean isProblem();
}
