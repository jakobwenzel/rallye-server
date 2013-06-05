package de.rallye.images;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PictureSize;

public class ImageRepository {
	
	
	private static final PictureSize THUMB = PictureSize.Thumbnail;
	private static final PictureSize STD = PictureSize.Standard;
	
	private static final Logger logger = LogManager.getLogger(ImageRepository.class);

	private String repository;
	private ImageCache thumbCache;
	private ImageCache stdCache;
	
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	
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

	
	public ImageRepository(String repository, int maxThumbEntries, int maxStdEntries) {
		this.repository = repository;
		this.thumbCache = new ImageCache(maxThumbEntries);
		this.stdCache = new ImageCache(maxStdEntries);
		
		new File(this.repository).mkdirs();
	}
	
	public BufferedImage get(int pictureID, PictureSize size) {
		logger.info("Requested "+ pictureID +" in "+ size);
		BufferedImage img = null;
		boolean cached = false;
		ImageCache cache = null;
		
		if (size == THUMB)
			cache = thumbCache;
		else if (size == STD)
			cache = stdCache;
		
		lock.readLock().lock();
		try {
			if (cache != null) {
				img = cache.get(pictureID);
				cached = (img != null);
			}
			
			if (!cached) {
				logger.info("Loading "+ pictureID +" from File");
				try {
					File f = getFile(pictureID, size);
				
					img = ImageIO.read(f);
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
	
	public void put(int pictureID, BufferedImage img) throws DataException {
		logger.info("Adding "+ pictureID +" to repository ("+ img.getWidth() +"x"+ img.getHeight() +")");
		
		
		lock.writeLock().lock();
		try {
			Dimension d;
			BufferedImage out;
			File f;//TODO: preserve EXIF + Meta-Data in original (requires getting img as IIOImage instead of BufferedImage)
			//TODO: save Meta-Data to DB ? location? provide API for displaying pictures in Map?
			
			for (PictureSize s: PictureSize.values()) {
				d = s.getDimension();
				out = (d != null)? ImageScaler.scaleImage(img, d) : img;
				
				if (s == THUMB)
					thumbCache.put(pictureID, out);
				else if (s == STD)
					stdCache.put(pictureID, out);
				
				f = getFile(pictureID, s);
				try {
					ImageIO.write(out, "jpg", f);
				} catch (IOException e) {
					final String msg = "Unable to write Image to disk";
					logger.error(msg, e);
					throw new DataException(msg, e);
				}
			}
			
		} finally {
			lock.writeLock().unlock();
		}
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
