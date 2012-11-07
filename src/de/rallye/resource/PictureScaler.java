package de.rallye.resource;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class PictureScaler {

	public static byte[] scale(byte[] fileData, int width, int height) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(fileData);

		BufferedImage img = ImageIO.read(in);
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

		return buffer.toByteArray();

	
	}
	
	
	public static byte[] scaleSameAspect(byte[] pic, int maxWidth, int maxHeight) throws IOException {
		int newHeight = 0, newWidth = 0;
		
		ByteArrayInputStream in = new ByteArrayInputStream(pic);
		BufferedImage img = ImageIO.read(in);
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

		return buffer.toByteArray();
	}

}
