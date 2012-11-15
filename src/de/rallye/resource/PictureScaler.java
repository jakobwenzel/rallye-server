package de.rallye.resource;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class PictureScaler {

	public static byte[] scale(byte[] fileData, int width, int height) throws IOException {
		Logger logger =  LogManager.getLogger(PictureScaler.class.getName());
		
		
		ByteArrayInputStream in = new ByteArrayInputStream(fileData);

		BufferedImage img = ImageIO.read(in);
		int i_w = img.getWidth(), i_h = img.getHeight();
		
		if (height == 0) {
			height = (width * img.getHeight()) / img.getWidth();
		}
		if (width == 0) {
			width = (height * img.getWidth()) / img.getHeight();
		}
		Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		ImageIO.write(imageBuff, "jpg", buffer);

		logger.info("Scale Pic from "+i_w+"x"+i_h+" ("+fileData.length+"Byte) to "+width+"x"+height+" ("+buffer.size()+" Byte)");
		
		return buffer.toByteArray();

	
	}
	
	
	public static byte[] scaleSameAspect(byte[] pic, int maxWidth, int maxHeight) throws IOException {
		Logger logger =  LogManager.getLogger(PictureScaler.class.getName());
		int newHeight = 0, newWidth = 0;
		
		ByteArrayInputStream in = new ByteArrayInputStream(pic);
		BufferedImage img = ImageIO.read(in);
		int i_w = img.getWidth(), i_h = img.getHeight();
		
		if (img.getHeight() >= img.getWidth()) {
			newHeight = maxHeight;
			newWidth = (maxHeight * img.getWidth()) / img.getHeight();
		} else {
			newWidth = maxWidth;
			newHeight = (maxWidth * img.getHeight()) / img.getWidth();
		}
		
		Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
		BufferedImage imageBuff = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		ImageIO.write(imageBuff, "jpg", buffer);

		logger.info("Scale Pic from "+i_w+"x"+i_h+" ("+pic.length+"Byte) to "+newWidth+"x"+newHeight+" ("+buffer.size()+" Byte)");
		
		return buffer.toByteArray();
	}

}
