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

import de.rallye.config.ImageCacheConfig;
import de.rallye.config.RallyeConfig;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Dimension;
import de.rallye.model.structures.PictureSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
@Service
public class ImageRepository {
	
	
	private static final PictureSize THUMB = PictureSize.Thumbnail;
	private static final PictureSize MINI = PictureSize.Mini;
	
	private static final Logger logger = LogManager.getLogger(ImageRepository.class);

	private final String repository;
	private final ImageCache thumbCache;
	private final ImageCache miniCache;
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	
	private static class ImageCache extends LinkedHashMap<Integer, BufferedImage> {
		
		private static final long serialVersionUID = 1L;
		
		private final int maxCacheEntries;
		
		public ImageCache(int maxCacheEntries) {
			super(maxCacheEntries, 1);
			this.maxCacheEntries = maxCacheEntries;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> eldest) {
			return size() > maxCacheEntries;
		}

	}
	
	private File getFile(int pictureID, PictureSize size) {
		return new File(repository, pictureID +"_"+ size.toShortString() +".jpg");
	}

	@Inject
	public ImageRepository(RallyeConfig config) {
		this.repository = config.getImageRepositoryPath();
		ImageCacheConfig cacheConfig = config.getImageCacheConfig();
		this.thumbCache = new ImageCache(cacheConfig.maxThumbEntries);
		this.miniCache = new ImageCache(cacheConfig.maxMiniEntries);

		new File(this.repository).mkdirs();
	}
	
	public BufferedImage get(int pictureID, PictureSize size) {
		logger.info("Requested "+ pictureID +" in "+ size);
		BufferedImage img = null;
		boolean cached = false;
		ImageCache cache = null;
		
		if (size == THUMB)
			cache = thumbCache;
		else if (size == MINI)
			cache = miniCache;
		
		lock.readLock().lock();
		try {
			if (cache != null) {
				img = cache.get(pictureID);
				cached = (img != null);
			}
			
			if (!cached) {
				logger.info("Loading {} from File", pictureID);
				try {
					File f = getFile(pictureID, size);
					if (f.exists()) {
						img = ImageIO.read(f);
					} else {
						logger.info("Size {} does not exist, scaling from Original", size);
						f = getFile(pictureID, PictureSize.Original);
						BufferedImage org = ImageIO.read(f);
						img = scalePicture(org, pictureID, size);
					}
				} catch (IOException e) {
					logger.error("Picture does not exist", e);
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		
		if (cache != null && !cached) {
			lock.writeLock().lock();
			try {
				cache.put(pictureID, img);
			} finally {
				lock.writeLock().unlock();
			}
		}
		return img;
	}
	
	public void put(int pictureID, File fIn) throws DataException {
		logger.info("Adding {} to repository", pictureID);
		//TODO: save Meta-Data to DB ? location? provide API for displaying pictures in Map?
		
		lock.writeLock().lock();
		try {
			BufferedImage iOut;
			File fOut;
			
			fOut = getFile(pictureID, PictureSize.Original);
			FileInputStream inStream = new FileInputStream(fIn);
			FileOutputStream outStream = new FileOutputStream(fOut);
			
			byte[] buffer = new byte[ 0xFFFF ];
		    for ( int len; (len = inStream.read(buffer)) != -1; ) {
		      outStream.write( buffer, 0, len );
		    }
		    outStream.close();
		    inStream.close();
		    
		    BufferedImage iIn = ImageIO.read(fIn);
		    
		    logger.info("Original saved ({}x{})", iIn.getWidth(), iIn.getHeight());
			
			for (PictureSize s: PictureSize.values()) {
				if (s == PictureSize.Original)
					continue;
				
				iOut = scalePicture(iIn, pictureID, s);
				
				if (s == THUMB)
					thumbCache.put(pictureID, iOut);
				else if (s == MINI)
					miniCache.put(pictureID, iOut);
			}
			
		} catch (FileNotFoundException e) {
			final String msg = "Could not find file"+ fIn.toString();
			logger.error(msg, e);
			throw new DataException(msg, e);
		} catch (IOException e) {
			final String msg = "Could not read/write";
			logger.error(msg, e);
			throw new DataException(msg, e);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	private BufferedImage scalePicture(BufferedImage base, int pictureID, PictureSize size) throws IOException {
		Dimension d = size.getDimension();
		
		BufferedImage out = ImageScaler.scaleImage(base, d);
		
		File f = getFile(pictureID, size);
		
		ImageIO.write(out, "jpg", f);
		
		logger.info("Scaled {} to {}", pictureID, size);
		
		return out;
	}
	
	public void remove(int pictureID) {
		logger.info("Removing all variants of "+ pictureID);
		
		lock.writeLock().lock();
		try {
			thumbCache.remove(pictureID);
			
			for (PictureSize s: PictureSize.values()) {
				File f = getFile(pictureID, s);
				f.delete();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}	
	
}
