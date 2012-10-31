package com.surelogic.common.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.CommonImages;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * A utility to manage and share images used by SureLogic plug-ins. It handles
 * both simple and decorated images. Most calls manage disposal and caching of
 * images so client code can be simpler. Public calls that are not managed have
 * the suffix <tt>Unmanaged</tt>
 * <p>
 * All image name refer to names in {@link CommonImages}. Pass one of the
 * <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared in that
 * utility.
 * 
 * @see CommonImages
 */
@Utility
public final class SLImages {

  private SLImages() {
    // no instances
  }

  /**
   * Our map from cache keys to images&mdash;we cache them (for sharing) and can
   * dispose the images we create. Use {@link #addToImageCache(String, Image)}
   * rather than putting in entries directly&mdash;this allows disposal
   * registration to be done properly.
   * <p>
   * A key is created using one of the three following calls:
   * <ul>
   * <li>{@link #getNameCacheKey(String, boolean)}</li>
   * <li>
   * {@link #getNameDecoratedCacheKey(String, ImageDescriptor[], Point, boolean)}
   * </li>
   * <li>
   * {@link #getImageDecoratedCacheKey(Image, ImageDescriptor[], Point, boolean)}
   * </li>
   * <li>{@link #getImageResizeCacheKey(Image, Point)}</li>
   * </ul>
   * <p>
   * The keys can have {@link #GRAY} on them to indicate the image is grayscale.
   */
  private static final Map<String, Image> CACHEKEY_TO_IMAGE = new HashMap<String, Image>();

  /**
   * Adds the passed image to the cache under the passed key.
   * 
   * @param key
   *          a key.
   * @param image
   *          an image.
   */
  private static void addToImageCache(@NonNull final String key, @NonNull Image image) {
    CACHEKEY_TO_IMAGE.put(key, image);
    registerDisposeExecIfNeeded();
  }

  private static boolean f_needToRegisterDisposeExec = true;

  /**
   * The first time this is called, and only the first time, code to run that
   * disposes the {@link Image} instances we manage is setup to be invoked by
   * the user-interface thread just before the current display is disposed.
   */
  private static void registerDisposeExecIfNeeded() {
    if (f_needToRegisterDisposeExec) {
      f_needToRegisterDisposeExec = false;
      Display.getCurrent().disposeExec(new Runnable() {
        public void run() {
          for (Image image : CACHEKEY_TO_IMAGE.values()) {
            image.dispose();
          }
          CACHEKEY_TO_IMAGE.clear();
        }
      });
    }
  }

  /**
   * Used in cache keys.
   */
  private static final String GRAY = "gray";

  /*
   * Key creation methods
   */

  @NonNull
  private static String getNameCacheKey(@NonNull String imageName, boolean grayscale) {
    final String key = imageName + (grayscale ? ":" + GRAY : "") + "->named";
    return key;
  }

  @NonNull
  private static String getNameDecoratedCacheKey(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray,
      boolean grayscale) {
    final int prime = 31;
    int intHash = 1;
    for (ImageDescriptor id : overlaysArray)
      intHash = prime * intHash + ((id == null) ? 0 : id.hashCode());
    final String key = imageName + ":" + Integer.toHexString(intHash) + (grayscale ? ":" + GRAY : "") + "->name-decorated";
    return key;
  }

  @NonNull
  private static String getImageDecoratedCacheKey(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray,
      @Nullable Point size, boolean grayscale) {
    if (size == null)
      size = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
    final int prime = 31;
    int intHash = 1;
    intHash = prime * intHash + ((baseImage == null) ? 0 : baseImage.hashCode());
    for (ImageDescriptor id : overlaysArray)
      intHash = prime * intHash + ((id == null) ? 0 : id.hashCode());
    final String key = Integer.toHexString(intHash) + "(" + size.x + "," + size.y + ")" + (grayscale ? GRAY : "")
        + "->image-decorated";
    return key;
  }

  @NonNull
  private static String getImageResizeCacheKey(@NonNull Image baseImage, @NonNull Point size) {
    final int prime = 31;
    int intHash = 1;
    intHash = prime * intHash + ((baseImage == null) ? 0 : baseImage.hashCode());
    final String key = Integer.toHexString(intHash) + "(" + size.x + "," + size.y + ")->image-resize";
    return key;
  }

  @NonNull
  private static String getImageIndentCacheKey(@NonNull Image baseImage, int pixelsFromRight, int pixelsFromTop) {
    final int prime = 31;
    int intHash = 1;
    intHash = prime * intHash + ((baseImage == null) ? 0 : baseImage.hashCode());
    final String key = Integer.toHexString(intHash) + "(" + pixelsFromRight + "," + pixelsFromTop + ")->image-indent";
    return key;
  }

  /**
   * Constructs a color or grayscale image for the passed name from
   * {@link CommonImages}.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param grayscale
   *          {@code true} if the returned image should be made grayscale,
   *          {@code false} for color.
   * @return a new image, or {@code null} if the image could not be created. The
   *         caller is responsible for disposing this image.
   */
  @Nullable
  private static Image createImage(final String imageName, boolean grayscale) {
    if (imageName != null) {
      final ImageDescriptor imageDescriptor = getImageDescriptor(imageName);
      if (imageDescriptor != null) {
        final Image result = imageDescriptor.createImage();
        if (grayscale && result != null) {
          final Image grayResult = toGray(result);
          result.dispose();
          return grayResult;
        }
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the image descriptor for the passed name from {@link CommonImages}.
   * <p>
   * Unlike an {@link Image} instance, the returned image descriptor does not
   * need to be disposed.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @return the image descriptor, or {@code null} if not found.
   */
  @Nullable
  public static ImageDescriptor getImageDescriptor(final String imageName) {
    if (imageName == null) {
      return null;
    }
    ImageDescriptor result = null;
    URL url = CommonImages.getImageURL(imageName);
    if (url != null) {
      result = ImageDescriptor.createFromURL(url);
      if (result != null) {
        return result;
      }
    }
    return result;
  }

  /**
   * Returns the shared color or grayscale image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param grayscale
   *          {@code true} if the returned image should be made grayscale,
   *          {@code false} for color.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   */
  @Nullable
  public static Image getImage(final String imageName, boolean grayscale) {
    if (imageName == null) {
      return null;
    }
    final String key = getNameCacheKey(imageName, grayscale);
    Image result = CACHEKEY_TO_IMAGE.get(key);
    if (result == null) {
      result = createImage(imageName, grayscale);
      if (result != null)
        addToImageCache(key, result);
    }
    return result;
  }

  /**
   * Returns the shared image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   */
  @Nullable
  public static Image getImage(final String imageName) {
    return getImage(imageName, false);
  }

  /**
   * Returns the shared grayscale image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   */
  @Nullable
  public static Image getGrayscaleImage(final String imageName) {
    return getImage(imageName, true);
  }

  /**
   * Creates a color or grayscale decorated image.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @param grayscale
   *          {@code true} if the returned image should be made grayscale,
   *          {@code false} for color.
   * @return a new image, or {@code null} if the image could not be created. The
   *         caller is responsible for disposing this image.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  private static Image createDecoratedImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray,
      @Nullable Point size, boolean grayscale) {
    if (baseImage == null)
      throw new IllegalArgumentException(I18N.err(44, "baseImage"));
    if (overlaysArray == null)
      throw new IllegalArgumentException(I18N.err(44, "overlaysArray"));
    if (overlaysArray.length != 5)
      throw new IllegalArgumentException("overlaysArray must have exactly 5 elements, some may be null: "
          + Arrays.toString(overlaysArray));
    if (size == null)
      size = new Point(baseImage.getBounds().width, baseImage.getBounds().height);

    final int baseImageWidth = baseImage.getBounds().width;
    int pixelsFromRight = 0;
    if (baseImageWidth != size.x)
      pixelsFromRight = (int) (((double) (size.x - baseImageWidth)) / 2.0);
    final boolean indentImage = pixelsFromRight > 0;
    final Image base = indentImage ? SLImages.indentImageUnmanaged(baseImage, pixelsFromRight, 0) : baseImage;
    final DecorationOverlayIcon doi = new DecorationOverlayIcon(base, overlaysArray, size);
    final Image result = doi.createImage();
    if (indentImage)
      base.dispose();
    if (grayscale && result != null) {
      final Image grayResult = toGray(result);
      result.dispose();
      return grayResult;
    }
    return result;
  }

  /**
   * Returns the shared color or grayscale decorated image managed under the
   * given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @param grayscale
   *          {@code true} if the returned image should be made grayscale,
   *          {@code false} for color.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if the image could
   *         not be created.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray, @Nullable Point size,
      boolean grayscale) {
    final String key = getImageDecoratedCacheKey(baseImage, overlaysArray, size, grayscale);
    Image result = CACHEKEY_TO_IMAGE.get(key);
    if (result == null) {
      result = createDecoratedImage(baseImage, overlaysArray, size, grayscale);
      if (result != null)
        addToImageCache(key, result);
    }
    return result;
  }

  /**
   * Returns the shared color decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if the image could
   *         not be created.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray, @Nullable Point size) {
    return getDecoratedImage(baseImage, overlaysArray, size, false);
  }

  /**
   * Returns the shared color decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if the image could
   *         not be created.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray) {
    return getDecoratedImage(baseImage, overlaysArray, null, false);
  }

  /**
   * Returns the shared grayscale decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if the image could
   *         not be created.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedGrayscaleImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray,
      @Nullable Point size) {
    return getDecoratedImage(baseImage, overlaysArray, size, true);
  }

  /**
   * Returns the shared grayscale decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param baseImage
   *          the base image.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if the image could
   *         not be created.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedGrayscaleImage(@NonNull Image baseImage, @NonNull ImageDescriptor[] overlaysArray) {
    return getDecoratedImage(baseImage, overlaysArray, null, true);
  }

  /**
   * Returns the shared color or grayscale decorated image managed under the
   * given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @param grayscale
   *          {@code true} if the returned image should be made grayscale,
   *          {@code false} for color.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray, @Nullable Point size,
      boolean grayscale) {
    final String key = getNameDecoratedCacheKey(imageName, overlaysArray, grayscale);
    Image result = CACHEKEY_TO_IMAGE.get(key);
    if (result == null) {
      final Image baseImage = getImage(imageName, false);
      if (baseImage != null) {
        result = createDecoratedImage(baseImage, overlaysArray, size, grayscale);
        if (result != null)
          addToImageCache(key, result);
      }
    }
    return result;
  }

  /**
   * Returns the shared color decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray, @Nullable Point size) {
    return getDecoratedImage(imageName, overlaysArray, size, false);
  }

  /**
   * Returns the shared color decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedImage(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray) {
    return getDecoratedImage(imageName, overlaysArray, null, false);
  }

  /**
   * Returns the shared grayscale decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * <p>
   * Note that the base image is centered horizontally in the resulting image.
   * This method uses {@link #indentImage(Image, int)} to shift the image.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @param size
   *          the size of the resulting image, or {@code null} if the resulting
   *          image should be the same size as the base image.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedGrayscaleImage(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray,
      @Nullable Point size) {
    return getDecoratedImage(imageName, overlaysArray, size, true);
  }

  /**
   * Returns the shared grayscale decorated image managed under the given name.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param imageName
   *          the name of the image. This should be one of the
   *          <tt>SharedImages.IMG_*</tt> or <tt>DECR_*</tt> constants declared
   *          in {@link CommonImages}.
   * @param overlaysArray
   *          the 5 overlay images. The indices of the array correspond to the
   *          values of the 5 overlay constants defined on {@link IDecoration} (
   *          {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
   *          {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
   *          and {@link IDecoration#UNDERLAY}). Some of these may be
   *          {@code null}.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws IllegalArgumentException
   *           if any of the arguments is invalid.
   * 
   * @see DecorationOverlayIcon
   */
  @Nullable
  public static Image getDecoratedGrayscaleImage(@NonNull String imageName, @NonNull ImageDescriptor[] overlaysArray) {
    return getDecoratedImage(imageName, overlaysArray, null, true);
  }

  /**
   * Constructs a new grayscale image (that must be disposed) from the passed
   * image. The returned image is the same size as the passed image.
   * 
   * @param image
   *          an image.
   * @return a grayscale version of <tt>image</tt>. The caller is responsible
   *         for disposing this image.
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  private static Image toGray(@NonNull final Image image) {
    return new Image(Display.getCurrent(), image, SWT.IMAGE_GRAY);
  }

  /**
   * Returns the shared image that is indented <tt>indentX</tt> pixels from the
   * left-hand side and <tt>indentY</tt> pixels from the top. The returned
   * image's size is expanded to perfectly fit the indented image..
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this method
   * <p>
   * if both <tt>indentX</tt> and <tt>indentY</tt> are zero or less an exact
   * copy of the passed image is returned.
   * 
   * @param baseImage
   *          an image.
   * @param pixelsFromRight
   *          the nonnegative number of pixels to indent in the <i>x</i>
   *          direction from the left-hand side of the image.
   * @param pixelsFromTop
   *          the nonnegative number of pixels to indent in the <i>y</i>
   *          direction from the top of the image.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static Image indentImage(@NonNull final Image baseImage, int pixelsFromRight, int pixelsFromTop) {
    if (baseImage == null)
      throw new IllegalArgumentException(I18N.err(44, "baseImage"));

    final String key = getImageIndentCacheKey(baseImage, pixelsFromRight, pixelsFromTop);
    Image result = CACHEKEY_TO_IMAGE.get(key);
    if (result == null) {
      result = indentImageUnmanaged(baseImage, pixelsFromRight, pixelsFromTop);
      if (result != null)
        addToImageCache(key, result);
    }
    return result;
  }

  /**
   * Constructs a new image indented <tt>indentX</tt> pixels from the left-hand
   * side and <tt>indentY</tt> pixels from the top. The returned image's size is
   * expanded to perfectly fit the indented image.
   * <p>
   * if both <tt>indentX</tt> and <tt>indentY</tt> are zero or less an exact
   * copy of the passed image is returned.
   * 
   * @param baseImage
   *          an image.
   * @param pixelsFromRight
   *          the nonnegative number of pixels to indent in the <i>x</i>
   *          direction from the left-hand side of the image.
   * @param pixelsFromTop
   *          the nonnegative number of pixels to indent in the <i>y</i>
   *          direction from the top of the image.
   * @return a new image. The caller is responsible for disposing this image.
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static Image indentImageUnmanaged(@NonNull final Image baseImage, int pixelsFromRight, int pixelsFromTop) {
    if (baseImage == null)
      throw new IllegalArgumentException(I18N.err(44, "baseImage"));

    if (pixelsFromRight < 1 && pixelsFromTop < 1)
      return new Image(Display.getCurrent(), baseImage, SWT.IMAGE_COPY);
    if (pixelsFromRight < 1)
      pixelsFromRight = 0;
    if (pixelsFromTop < 1)
      pixelsFromTop = 0;

    final Point size = new Point(baseImage.getBounds().width + pixelsFromRight, baseImage.getBounds().height + pixelsFromTop);
    final ImageDescriptor id = getImageDescriptorFor(baseImage);
    final DecorationOverlayIcon doi = new DecorationOverlayIcon(getImage(CommonImages.IMG_TRANSPARENT_PIXEL),
        new ImageDescriptor[] { null, null, null, id, null }, size);
    final Image result = doi.createImage();
    return result;
  }

  /**
   * Returns the shared image that is a resized copy, at least what will fit, of
   * the passed image with the passed size.
   * <p>
   * Note that clients <b>must not</b> dispose the image returned by this
   * method.
   * 
   * @param baseImage
   *          an image.
   * @param size
   *          size of the new image. If this exactly matches the size of the
   *          passed image then an exact copy of the passed image is returned.
   * @return an image that is managed by this utility, please do not call
   *         {@link Image#dispose()} on it, or {@code null} if
   *         <tt>imageName</tt> could not be found.
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  public static Image resizeImage(@NonNull final Image baseImage, @NonNull final Point size) {
    if (baseImage == null)
      throw new IllegalArgumentException(I18N.err(44, "baseImage"));
    if (size == null)
      throw new IllegalArgumentException(I18N.err(44, "size"));

    final String key = getImageResizeCacheKey(baseImage, size);
    Image result = CACHEKEY_TO_IMAGE.get(key);
    if (result == null) {
      result = resizeImageUnmanaged(baseImage, size);
      if (result != null)
        addToImageCache(key, result);
    }
    return result;
  }

  /**
   * Constructs a new image of the passed size coping the contents, at least
   * what will fit, of the passed base image to the new image.
   * 
   * @param baseImage
   *          an image.
   * @param size
   *          size of the new image. If this exactly matches the size of the
   *          passed image then an exact copy of the passed image is returned.
   * @return a new image. The caller is responsible for disposing this image.
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @NonNull
  public static Image resizeImageUnmanaged(@NonNull final Image baseImage, @NonNull final Point size) {
    if (baseImage == null)
      throw new IllegalArgumentException(I18N.err(44, "baseImage"));
    if (size == null)
      throw new IllegalArgumentException(I18N.err(44, "size"));

    if (baseImage.getBounds().width == size.x && baseImage.getBounds().height == size.y)
      return new Image(Display.getCurrent(), baseImage, SWT.IMAGE_COPY);
    final DecorationOverlayIcon doi = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] { null, null, null, null, null },
        size);
    final Image result = doi.createImage();
    return result;
  }

  /**
   * Creates and returns a new image descriptor for the given image. Note that
   * disposing the original Image will cause the descriptor to become invalid.
   * 
   * @param image
   *          an image.
   * @return a newly created image descriptor.
   * 
   * @throws Exception
   *           if something goes wrong.
   * 
   * @see ImageDescriptor#createFromImage(Image)
   */
  @NonNull
  public static ImageDescriptor getImageDescriptorFor(@NonNull final Image image) {
    return ImageDescriptor.createFromImage(image);
  }

  /**
   * Gets a copy of the image cache managed by this utility. This method is
   * primarily intended for testing and debug views.
   * 
   * @return a copy of the image cache managed by this utility.
   */
  @NonNull
  public static HashMap<String, Image> getCopyOfImageCache() {
    return new HashMap<String, Image>(CACHEKEY_TO_IMAGE);
  }

  @NonNull
  public static Image getImageForProject(@Nullable String projectName, @Nullable Point size) {
    final Image base = getImage(CommonImages.IMG_PROJECT);
    if (base == null)
      throw new IllegalStateException("CommonImages.IMG_PROJECT does not exist");
    if (SLUtility.isNotEmptyOrNull(projectName) && !projectName.startsWith(SLUtility.LIBRARY_PROJECT)
        && !projectName.startsWith(SLUtility.UNKNOWN_PROJECT)) {
      try {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot wsRoot = ws.getRoot();
        final IProject project = wsRoot.getProject(projectName);
        if (project != null && project.exists()) {
          if (project.hasNature(SLUtility.ANDROID_NATURE)) {
            return getDecoratedImage(base, new ImageDescriptor[] { null, getImageDescriptor(CommonImages.DECR_ANDROID), null, null,
                null }, size);
          } else if (project.hasNature(SLUtility.JAVA_NATURE)) {
            return getDecoratedImage(base, new ImageDescriptor[] { null, getImageDescriptor(CommonImages.DECR_JAVA), null, null,
                null }, size);
          }
        }
      } catch (Exception ignore) {
        // just return the base project image
      }
    }
    return base;
  }

  @NonNull
  public static Image getImageForProject(@Nullable String projectName) {
    return getImageForProject(projectName, null);
  }
}
