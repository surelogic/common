package com.surelogic.common.refactor;

import java.util.*;

/**
 * Describes an annotation that should be placed at a given target.
 */
public class AnnotationDescription implements Comparable<AnnotationDescription> {

	private final String annotation;
	private final String contents;
	private final IJavaDeclaration target;
	private final IJavaDeclaration assumptionTarget;
	private final CU cu;
	private final CU assumptionCU;

	public AnnotationDescription(final String annotation,
			final String contents, final IJavaDeclaration target) {
		this(annotation, contents, target, null, null, null);
	}

	public AnnotationDescription(final String annotation,
			final String contents, final IJavaDeclaration target,
			final IJavaDeclaration assumptionTarget, final CU cu,
			final CU assumptionCU) {
		if (annotation == null) {
			throw new IllegalArgumentException(
					"The annotation must always be specified.");
		}
		if (target == null) {
			throw new IllegalArgumentException(
					"A target for this annotation must be specified");
		}
		this.target = target;
		this.assumptionTarget = assumptionTarget;
		this.annotation = annotation;
		this.contents = contents;
		this.cu = cu;
		this.assumptionCU = assumptionCU;
	}

	public IJavaDeclaration getTarget() {
		return target;
	}

	public IJavaDeclaration getAssumptionTarget() {
		return assumptionTarget;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getContents() {
		return contents;
	}

	@Override
	public String toString() {
		if (getContents() == null) {
			return String.format("@%s", getAnnotation());
		}
		return String.format("@%s(%s)", getAnnotation(), getContents());
	}

	public int compareTo(final AnnotationDescription o) {
		int compare = cmp.compare(getAnnotation(), o.getAnnotation());
		if (compare == 0) {
			if (contents == null && o.contents == null) {
				compare = 0;
			} else if (contents == null) {
				compare = -1;
			} else if (o.contents == null) {
				compare = 1;
			} else {
				compare = o.contents.compareTo(contents);
			}
		}
		return compare;
	}

	public boolean hasContents() {
		return getContents() != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (annotation == null ? 0 : annotation.hashCode());
		result = prime * result + (contents == null ? 0 : contents.hashCode());
		result = prime * result + (target == null ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AnnotationDescription other = (AnnotationDescription) obj;
		if (annotation == null) {
			if (other.annotation != null) {
				return false;
			}
		} else if (!annotation.equals(other.annotation)) {
			return false;
		}
		if (contents == null) {
			if (other.contents != null) {
				return false;
			}
		} else if (!contents.equals(other.contents)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

	public CU getCU() {
		return cu;
	}

	public CU getAssumptionCU() {
		return assumptionCU;
	}

	public static class CU {
		final String proj;
		final String cu;
		final String pakkage;

		public CU(final String p, final String pakkage, final String cu) {
			proj = p;
			if (cu == null) {
				throw new IllegalArgumentException();
			}
			final int lastSeparator = cu.lastIndexOf('/');
			if (lastSeparator >= 0) {
				this.cu = cu.substring(lastSeparator+1);
			} else {
				this.cu = cu;
			}
			this.pakkage = pakkage;
		}

		public String getProject() {
			return proj;
		}
 		
		public String getCu() {
			return cu;
		}

		public String getPackage() {
			return pakkage;
		}

		@Override
		public String toString() {
			return pakkage + "." + cu;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (cu == null ? 0 : cu.hashCode());
			result = prime * result
					+ (pakkage == null ? 0 : pakkage.hashCode());
			result = prime * result
			        + (proj == null ? 0 : proj.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CU other = (CU) obj;
			if (cu == null) {
				if (other.cu != null) {
					return false;
				}
			} else if (!cu.equals(other.cu)) {
				return false;
			}
			if (pakkage == null) {
				if (other.pakkage != null) {
					return false;
				}
			} else if (!pakkage.equals(other.pakkage)) {
				return false;
			}
			if (proj == null) {
				if (other.proj != null) {
					return false;
				}
			} else if (!proj.equals(other.proj)) {
				return false;
			}
			return true;
		}
	}

	private static final Comparator<String> cmp = new Comparator<String>() {
		final List<String> custom = Arrays.asList("Unique", "Aggregate",
				"AggregateInRegion", "Region", "Regions", "RegionLock",
				"RegionLocks");

		public int compare(final String o1, final String o2) {
			final int indexOf1 = custom.indexOf(o1);
			final int indexOf2 = custom.indexOf(o2);
			if (indexOf1 == -1 && indexOf2 == -1) {
				return o1.compareTo(o2);
			} else if (indexOf1 == -1) {
				return -1;
			} else if (indexOf2 == -1) {
				return 1;
			} else {
				return indexOf1 - indexOf2;
			}
		}
	};
}
