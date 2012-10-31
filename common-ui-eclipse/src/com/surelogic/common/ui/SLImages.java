package com.surelogic.common.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.surelogic.NonNull;
import com.surelogic.Utility;
import com.surelogic.common.CommonImages;

/**
 * A utility to manage and share images used by SureLogic plug-ins.
 */
@Utility
public final class SLImages {

  private SLImages() {
    // no instances
  }

  /**
   * Our map from keys to images so that we cache them (for sharing) and can
   * dispose of what we create.
   */
  private static final Map<String, Image> f_keyToImage = new HashMap<String, Image>();

  private static boolean f_needToRegDisposeExec = true;

  /**
   * Returns the shared image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param symbolicName
   *          the symbolic name of the image; one of the
   *          <code>SharedImages.IMG_*</code> constants declared in
   *          {@link CommonImages}.
   * @return the image, or <code>null</code> if not found.
   */
  public static Image getImage(final String symbolicName) {
    if (symbolicName == null) {
      return null;
    }
    Image result = f_keyToImage.get(symbolicName);
    if (result != null) {
      return result;
    } else {
      final ImageDescriptor imageDescriptor = getImageDescriptor(symbolicName);
      if (imageDescriptor != null) {
        result = imageDescriptor.createImage();
        if (result != null) {
          f_keyToImage.put(symbolicName, result);
          disposeExec();
          return result;
        }
      }
    }
    return result;
  }

  /**
   * Returns the image descriptor managed under the given name.
   * <p>
   * Unlike {@link Image}s, image descriptors themselves do not need to be
   * disposed.
   * 
   * @param symbolicName
   *          the symbolic name of the image; one of the
   *          <code>SharedImages.IMG_*</code> constants declared in this class.
   * @return the image descriptor, or <code>null</code> if not found.
   */
  public static ImageDescriptor getImageDescriptor(final String symbolicName) {
    if (symbolicName == null) {
      return null;
    }
    ImageDescriptor result = null;
    URL url = CommonImages.getImageURL(symbolicName);
    if (url != null) {
      result = ImageDescriptor.createFromURL(url);
      if (result != null) {
        return result;
      }
    }
    return result;
  }

  /**
   * Constructs a new grayscale image (that must be disposed) from the passed
   * image. The returned image is the same size as the passed image.
   * 
   * @param image
   *          an image.
   * @return a grayscale version of <tt>image</tt>. The caller is responsible
   *         for disposing this image.
   */
  @NonNull
  public static Image toGray(@NonNull final Image image) {
    return new Image(image.getDevice(), image, SWT.IMAGE_GRAY);
  }

  /**
   * Constructs a new image indented <tt>pixelsToIndent</tt>. The returned
   * image's width is expanded <tt>pixelsToIndent</tt>.
   * 
   * @param baseImage
   *          an image.
   * @param pixelsToIndent
   *          the number of pixels to indent in the <i>x</i> direction. This
   *          value must be greater than zero or an exact copy of the passed
   *          image is returned.
   * @return a new image. The caller is responsible for disposing this image.
   */
  @NonNull
  public static Image indentImage(@NonNull final Image baseImage, final int pixelsToIndent) {
    if (pixelsToIndent < 1)
      return new Image(baseImage.getDevice(), baseImage.getImageData(), baseImage.getImageData().getTransparencyMask());
    final ImageData data = baseImage.getImageData();
    final ImageData mask = data.getTransparencyMask();
    final ImageData newData = new ImageData(data.width + pixelsToIndent, data.height, data.depth, data.palette);
    final ImageData newMask = new ImageData(data.width + pixelsToIndent, data.height, data.depth, mask.palette);
    for (int y = 0; y < newData.height; y++) {
      for (int x = 0; x < newData.width; x++) {
        final int xOld = x - pixelsToIndent;
        if (xOld >= 0) {
          newData.setPixel(x, y, data.getPixel(xOld, y));
          newMask.setPixel(x, y, mask.getPixel(xOld, y));
        }
      }
    }
    final Image result = new Image(baseImage.getDevice(), newData, newMask);
    return result;
  }

  private static void disposeExec() {
    if (f_needToRegDisposeExec) {
      f_needToRegDisposeExec = false;
      Display.getCurrent().disposeExec(new Runnable() {
        public void run() {
          for (Image image : f_keyToImage.values()) {
            image.dispose();
          }
          f_keyToImage.clear();
        }
      });
    }
  }
}
