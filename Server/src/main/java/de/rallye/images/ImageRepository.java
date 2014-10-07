/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.images;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.rallye.config.ImageCacheConfig;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Dimension;
import de.rallye.model.structures.LatLngAlt;
import de.rallye.model.structures.PictureSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
@Service
public class ImageRepository implements IPictureRepository {
	
	
	private static final PictureSize THUMB = PictureSize.Thumbnail;
	private static final PictureSize MINI = PictureSize.Mini;
	
	private static final Logger logger = LogManager.getLogger(ImageRepository.class);

	private final String repository;
	private final ImageCache thumbCache;
	private final ImageCache miniCache;
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final IDataAdapter data;

	@Inject
	public ImageRepository(RallyeConfig config, IDataAdapter data) {
		this.repository = config.getImageRepositoryPath();
		this.data = data;
		ImageCacheConfig cacheConfig = config.getImageCacheConfig();
		this.thumbCache = new ImageCache(cacheConfig.maxThumbEntries);
		this.miniCache = new ImageCache(cacheConfig.maxMiniEntries);

		new File(this.repository).mkdirs();
	}

	@Override
	public Picture getImage(String pictureHash) {
		return new Picture(pictureHash);
	}

	@Override
	public Picture putImage(int userID, String pictureHash, File file) throws DataException {
		return putImage(userID, pictureHash, file, PictureSize.Original);
	}

	private void scalePreemptively(File src, String pictureHash, PictureSize supplied) {//TODO exclude everything larger than the supplied size
		for (PictureSize s: PictureSize.values()) {
			if (s == supplied)
				continue;

			try {
				scalePicture(src, pictureHash, s);
			} catch (IOException e) {
				logger.error("Failed to scale picture to {}", s, e);
			}
		}
	}

	@Override
	public Picture putImagePreview(int userID, String pictureHash, File file) throws DataException {
		return putImage(userID, pictureHash, file, PictureSize.Preview);
	}

	private Picture putImage(int userID, String pictureHash, File file, PictureSize size) throws DataException {
		logger.info("Adding {} to repository as {}", size, pictureHash);
		lock.writeLock().lock();
		try {

			Metadata meta = null;
			try {
				meta = ImageMetadataReader.readMetadata(file);
			} catch (ImageProcessingException e) {
				logger.error("Failed to extract Meta information", e);
			}
			int pictureID = data.addPicture(userID, pictureHash, meta);

			BufferedImage iOut;
			File fOut;

			fOut = getFile(pictureHash, size);
			FileInputStream inStream = new FileInputStream(file);
			FileOutputStream outStream = new FileOutputStream(fOut);

			copy(inStream, outStream);

			outStream.close();
			inStream.close();

//			scalePreemptively(fOut, pictureHash, size);
			return new Picture(pictureHash, pictureID);
		} catch (FileNotFoundException e) {
			final String msg = "Lost uploaded file"+ file.toString();
			logger.error(msg, e);
			throw new WebApplicationException(msg);
		} catch (IOException e) {
			final String msg = "Could not read/write";
			logger.error(msg, e);
			throw new WebApplicationException(msg, e);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private static void copy(InputStream inStream, OutputStream outStream) throws IOException {
		byte[] buffer = new byte[ 0xFFFF ];
		for ( int len; (len = inStream.read(buffer)) != -1; ) {
			outStream.write( buffer, 0, len );
		}
	}

	@Override
	public void remove(String pictureHash) {
		logger.info("Removing all variants of "+ pictureHash);

		lock.writeLock().lock();
		try {
			thumbCache.remove(pictureHash);
			miniCache.remove(pictureHash);

			for (PictureSize s: PictureSize.values()) {
				File f = getFile(pictureHash, s);
				f.delete();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}


	public static LatLngAlt readGps(Metadata meta) {
		GpsDirectory gps = meta.getDirectory(GpsDirectory.class);
		if (gps == null)
			return null;

		int altitude;
		try {
			altitude = gps.getInt(GpsDirectory.TAG_GPS_ALTITUDE);
		} catch (MetadataException e) {
			e.printStackTrace();
			altitude = 0;
		}

		LatLngAlt location = null;
		try {
			location = new LatLngAlt(gps.getGeoLocation().getLatitude(), gps.getGeoLocation().getLongitude(), altitude);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public static String readMakeModel(Metadata meta) {
		StringBuilder sb = new StringBuilder();

		ExifIFD0Directory exif = meta.getDirectory(ExifIFD0Directory.class);

		if (exif == null) {
			return null;
		}

		sb.append(exif.getString(ExifIFD0Directory.TAG_MAKE)).append(" - ").append(exif.getString(ExifIFD0Directory.TAG_MODEL));

		return sb.toString();
	}

	private static class ImageCache extends LinkedHashMap<String, byte[]> {
		
		private static final long serialVersionUID = 1L;
		
		private final int maxCacheEntries;
		
		public ImageCache(int maxCacheEntries) {
			super(maxCacheEntries, 1);
			this.maxCacheEntries = maxCacheEntries;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
			return size() > maxCacheEntries;
		}

	}
	
	private File getFile(String pictureHash, PictureSize size) {
		return new File(repository, pictureHash +"_"+ size.toShortString() +".jpg");
	}
	
	private void scalePicture(File src, String pictureHash, PictureSize size) throws IOException {
		lock.writeLock().lock();
		try {
			Dimension d = size.getDimension();

			BufferedImage base = ImageIO.read(src);

			BufferedImage out = ImageScaler.scaleImage(base, d);

			File f = getFile(pictureHash, size);


			if (size == PictureSize.Mini || size == PictureSize.Thumbnail) {
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				ImageIO.write(out, "jpg", bStream);

				if (size == PictureSize.Thumbnail) {
					thumbCache.put(pictureHash, bStream.toByteArray());
				} else if (size == PictureSize.Mini) {
					miniCache.put(pictureHash, bStream.toByteArray());
				}

				ByteArrayInputStream iStream = new ByteArrayInputStream(bStream.toByteArray());
				BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(f));
				copy(iStream, outStream);
				iStream.close();
				outStream.close();

				bStream.close();
			} else {
				ImageIO.write(out, "jpg", f);
			}

			logger.info("Scaled {} to {} from ({}x{})", pictureHash, size, base.getWidth(), base.getHeight());
		} finally {
			lock.writeLock().unlock();
		}
	}

	public class Picture extends de.rallye.model.structures.Picture implements IPicture {

		private int pictureID = -1;

		public Picture(String pictureHash) {
			super(pictureHash);
		}

		public Picture(String pictureHash, int pictureID) {
			super(pictureHash);
			this.pictureID = pictureID;
		}

		@JsonIgnore
		@Override
		public int getPictureID() {
			return pictureID;
		}

		@Override
		public long lastModified() {
			return getMostOrgFile().lastModified();
		}

		private File getMostOrgFile() {
			lock.readLock().lock();
			try {
				File f = getOrgFile();
				if (f == null) {
					f = getPreviewFile();
				}
				return f;
			} finally {
				lock.readLock().unlock();
			}
		}

        @Override
        public File getUpToStdFile() {
            lock.readLock().lock();
            try {
                File f = null;
                try {
                    f = getFile(PictureSize.Standard);
                } catch (WebApplicationException e) { //thrown if there is no org uploaded yet
                    f = getPreviewFile();
                }
                return f;
            } finally {
                lock.readLock().unlock();
            }
        }

		private File getPreviewFile() {
			lock.readLock().lock();
			try {
				File f = ImageRepository.this.getFile(pictureHash, PictureSize.Preview);
				if (f.exists())
					return f;
				else
					return null;
			} finally {
				lock.readLock().unlock();
			}
		}

		private File getOrgFile() {
			lock.readLock().lock();
			try {
				File f = ImageRepository.this.getFile(pictureHash, PictureSize.Original);
				if (f.exists())
					return f;
				else
					return null;
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public boolean isAvailable(PictureSize size) {
			return isCached(size) || getFile(size).exists();
		}

		@Override
		public File getFile(PictureSize size) {
			lock.readLock().lock();
			File target;
			File org;
			try {
				target = ImageRepository.this.getFile(pictureHash, size);
				if (target.exists())
					return target;

				org = getOrgFile();
				PictureSize avail = PictureSize.Original;

				if (org == null) {
					org = getPreviewFile();
					avail = PictureSize.Preview;
				}

				if (!isScalableFrom(avail, size)) {
					throw new WebApplicationException("only a preview has been uploaded yet, you should fall back to preview and smaller", 409);
				}
			} finally {
				lock.readLock().unlock();
			}
			try {
				scalePicture(org, pictureHash, size);
			} catch (IOException e) {
				logger.error("Failed to scale picture", e);
			}
			return target;
		}

		@Override
		public boolean isCached(PictureSize size) {
			lock.readLock().lock();
			try {
				switch (size) {
					case Thumbnail:
						return thumbCache.containsKey(pictureHash);
					case Mini:
						return miniCache.containsKey(pictureHash);
					default:
						return false;
				}
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public byte[] getCached(PictureSize size) {
			lock.readLock().lock();
			try {
				switch (size) {
					case Thumbnail:
						return thumbCache.get(pictureHash);
					case Mini:
						return miniCache.get(pictureHash);
					default:
						return null;
				}
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public InputStream getInputStream(PictureSize size) throws FileNotFoundException {
			byte[] cached = getCached(size);
			if (cached != null) {
				return new ByteArrayInputStream(cached);
			} else {
				return new BufferedInputStream(new FileInputStream(getFile(size)));
			}
		}
	}

	private boolean isScalableFrom(PictureSize source, PictureSize target) {
		if (source == PictureSize.Original)
			return true;
		else if (source == PictureSize.Preview) {
			return target == PictureSize.Mini || target == PictureSize.Thumbnail;
		} else
			return false;// we only support scaling from preview / org, everything else does not seem sensible
	}
}
