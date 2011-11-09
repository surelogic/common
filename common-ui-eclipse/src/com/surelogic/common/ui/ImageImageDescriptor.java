package com.surelogic.common.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Image descriptor wrapper class for an existing instance of {@link Image}.
 */
public class ImageImageDescriptor extends ImageDescriptor {

	private Image f_Image;

	/**
	 * Constructor for ImageImageDescriptor.
	 */
	public ImageImageDescriptor(Image image) {
		super();
		f_Image = image;
	}

	public ImageData getImageData() {
		return f_Image.getImageData();
	}

	public boolean equals(Object obj) {
		return (obj != null) && getClass().equals(obj.getClass())
				&& f_Image.equals(((ImageImageDescriptor) obj).f_Image);
	}

	public int hashCode() {
		return f_Image.hashCode();
	}
}
