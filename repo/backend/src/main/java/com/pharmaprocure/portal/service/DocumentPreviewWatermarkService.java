package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.exception.ApiException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

@Service
public class DocumentPreviewWatermarkService {

    private static final String PDF = "application/pdf";
    private static final String PNG = "image/png";
    private static final String JPEG = "image/jpeg";

    public boolean supports(String mimeType) {
        return PDF.equals(mimeType) || PNG.equals(mimeType) || JPEG.equals(mimeType);
    }

    public byte[] apply(Path path, String mimeType, String watermarkText) {
        try {
            if (PDF.equals(mimeType)) {
                return watermarkPdf(path, watermarkText);
            }
            if (PNG.equals(mimeType) || JPEG.equals(mimeType)) {
                return watermarkImage(path, mimeType, watermarkText);
            }
        } catch (IOException ex) {
            throw new ApiException(500, "Unable to prepare preview content", List.of("PREVIEW_WATERMARK_FAILED"));
        }
        throw new ApiException(400, "Preview is unavailable for this file type", List.of("PREVIEW_NOT_SUPPORTED"));
    }

    private byte[] watermarkImage(Path path, String mimeType, String watermarkText) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new IOException("Unsupported image format");
        }
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        graphics.setColor(new Color(68, 68, 68));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(18, image.getWidth() / 18)));

        AffineTransform previous = graphics.getTransform();
        graphics.rotate(-Math.PI / 4, image.getWidth() / 2.0, image.getHeight() / 2.0);
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(watermarkText);
        int stepX = Math.max(textWidth + 80, image.getWidth() / 2);
        int stepY = Math.max(metrics.getHeight() + 80, image.getHeight() / 3);
        for (int x = -image.getWidth(); x < image.getWidth() * 2; x += stepX) {
            for (int y = 0; y < image.getHeight() * 2; y += stepY) {
                graphics.drawString(watermarkText, x, y);
            }
        }
        graphics.setTransform(previous);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, PNG.equals(mimeType) ? "png" : "jpg", output);
        return output.toByteArray();
    }

    private byte[] watermarkPdf(Path path, String watermarkText) throws IOException {
        try (PDDocument document = PDDocument.load(path.toFile()); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (PDPage page : document.getPages()) {
                float width = page.getMediaBox().getWidth();
                float height = page.getMediaBox().getHeight();
                PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                graphicsState.setNonStrokingAlphaConstant(0.18f);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
                    contentStream.setGraphicsStateParameters(graphicsState);
                    contentStream.setNonStrokingColor(90, 90, 90);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, Math.max(24f, width / 14f));
                    contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), width * 0.2f, height * 0.3f));
                    contentStream.showText(watermarkText);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12f);
                    contentStream.setTextMatrix(Matrix.getTranslateInstance(36f, 24f));
                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
