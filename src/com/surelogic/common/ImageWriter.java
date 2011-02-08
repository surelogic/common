package com.surelogic.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class that makes it easy to choose one or more images and write
 * them out to a given folder.
 * 
 * @author nathan
 * 
 */
public class ImageWriter {

	private final static String IMAGE_FOLDER = "image_files";

	private final Set<String> f_images = new HashSet<String>();
	private final File f_imageDir;
	private final String f_folderName;

	/**
	 * Constructs an writer that will write out images into the folder
	 * <code>parentDir/image_files</code>.
	 * 
	 * @param parentDir
	 */
	public ImageWriter(final File parentDir) {
		this(parentDir, IMAGE_FOLDER);
	}

	public ImageWriter(final File parentDir, final String relativePath) {
		f_imageDir = new File(parentDir, relativePath);
		f_folderName = relativePath;
	}

	/**
	 * Adds an image to the list of images to be written out
	 * 
	 * @param imageName
	 */
	public void addImage(final String imageName) {
		f_images.add(imageName);
	}

	/**
	 * Produce a string representing the relative path to this particular image.
	 * This is for use in URL's.
	 * 
	 * @param image
	 * @return
	 */
	public String imageLocation(final String image) {
		return f_folderName + '/' + image;
	}

	/**
	 * Produce an image tag pointing to the given image and alt text. The src
	 * attribute will be qualified by the relative path of the image writer,
	 * which is <code>image_files</code> by default.
	 * 
	 * @param image
	 * @param altText
	 * @return
	 */
	public String imageTag(final String image, final String altText) {
		return "<img src=\"" + imageLocation(image) + "\" alt=\"" + altText
				+ "\" />";
	}

	/**
	 * Copy all images that have been added to this writer to its target
	 * directory.
	 * 
	 * @return whether the copy fully succeeded
	 */
	public boolean writeImages() {
		if (!f_imageDir.exists()) {
			if (!f_imageDir.mkdirs()) {
				return false;
			}
		}
		boolean success = true;
		for (final String image : f_images) {
			success &= FileUtility.copy(CommonImages.getImageURL(image),
					new File(f_imageDir, image));
		}
		return success;
	}

}
