package Features;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class PdfExport
{
    private static PdfExport pdfExport;
    private PdfExport() {}

    public static PdfExport getInstance()
    {
        if (pdfExport == null)
        {
            pdfExport = new PdfExport();
        }
        return pdfExport;
    }


    // Expected keys in the `data` map. Any missing key falls
    // back to "N/A" in the generated PDF rather than throwing.
    public static final String KEY_NAME = "name";
    public static final String KEY_SETTING = "setting";
    public static final String KEY_TOTAL_STUDENTS = "totalStudents";
    public static final String KEY_TOTAL_SEATS = "totalSeats";

    private static final float MARGIN = 40f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();


    /**
     * Generates a PDF report containing the summary info in `data`,
     * a snapshot of the seating grid, and a snapshot of the student
     * list VBox.
     *
     * MUST be called on the JavaFX Application Thread, since it
     * snapshots live UI nodes — the actual file writing happens
     * synchronously here too, so for a large roster you may want to
     * show a loading indicator while this runs.
     *
     * @param data           summary fields; see KEY_* constants above
     * @param studentVBox    the VBox containing every student card
     * @param seatingGrid    the GridPane showing the current seating arrangement
     * @param savePdfAtPath  full file path (including .pdf) to write to
     */
    public void convertIntoPdf(
            Map<String, String> data,
            Node studentVBox,
            Node seatingGrid,
            String savePdfAtPath)
    {
        // Snapshots must be taken on the FX thread, so this happens
        // before any offloading.
        BufferedImage gridImage = captureNode(seatingGrid);
        BufferedImage vboxImage = captureNode(studentVBox);

        try (PDDocument document = new PDDocument())
        {
            addSummaryPage(document, data);

            if (gridImage != null)
            {
                addImageSection(document, gridImage, "Seating Arrangement");
            }

            if (vboxImage != null)
            {
                addImageSection(document, vboxImage, "Student List");
            }

            document.save(savePdfAtPath);

            System.out.println("PDF saved to " + savePdfAtPath);
        }
        catch (IOException e)
        {
            System.out.println("Could not create PDF.");
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------
    // Node -> image capture
    // ---------------------------------------------------------

    private BufferedImage captureNode(Node node)
    {
        if (node == null)
        {
            return null;
        }

        WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);

        return SwingFXUtils.fromFXImage(snapshot, null);
    }


    // ---------------------------------------------------------
    // Summary page (name, setting, totals)
    // ---------------------------------------------------------

    private void addSummaryPage(PDDocument document, Map<String, String> data) throws IOException
    {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        String name = data.getOrDefault(KEY_NAME, "N/A");
        String setting = data.getOrDefault(KEY_SETTING, "N/A");
        String totalStudents = data.getOrDefault(KEY_TOTAL_STUDENTS, "N/A");
        String totalSeats = data.getOrDefault(KEY_TOTAL_SEATS, "N/A");

        try (PDPageContentStream content = new PDPageContentStream(document, page))
        {
            float y = PAGE_HEIGHT - MARGIN;

            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Seating Report");
            content.endText();

            y -= 40;

            y = writeLine(content, "Name: " + name, y);
            y = writeLine(content, "Setting: " + setting, y);
            y = writeLine(content, "Total Students: " + totalStudents, y);
            y = writeLine(content, "Total Seats: " + totalSeats, y);
        }
    }

    private float writeLine(PDPageContentStream content, String text, float y) throws IOException
    {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        content.newLineAtOffset(MARGIN, y);
        content.showText(text);
        content.endText();

        return y - 20;
    }


    // ---------------------------------------------------------
    // Image sections (grid / student list), split across as
    // many pages as needed if the content is taller than one page
    // ---------------------------------------------------------

    private void addImageSection(PDDocument document, BufferedImage image, String heading) throws IOException
    {
        float maxContentWidth = PAGE_WIDTH - (2 * MARGIN);
        float maxContentHeight = PAGE_HEIGHT - (2 * MARGIN) - 30; // leave room for the heading

        // Scale the image to fit the page width; keep aspect ratio.
        float scale = maxContentWidth / image.getWidth();
        int scaledFullHeight = Math.round(image.getHeight() * scale);

        // How many source pixel rows fit in one page at this scale.
        int rowsPerPage = Math.max(1, Math.round(maxContentHeight / scale));

        int y = 0;
        boolean firstPage = true;

        while (y < image.getHeight())
        {
            int sliceHeight = Math.min(rowsPerPage, image.getHeight() - y);

            BufferedImage slice = image.getSubimage(0, y, image.getWidth(), sliceHeight);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            byte[] pngBytes = toPngBytes(slice);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, pngBytes, heading);

            try (PDPageContentStream content = new PDPageContentStream(document, page))
            {
                float pageY = PAGE_HEIGHT - MARGIN;

                if (firstPage)
                {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                    content.newLineAtOffset(MARGIN, pageY);
                    content.showText(heading);
                    content.endText();

                    pageY -= 30;
                    firstPage = false;
                }

                float drawWidth = slice.getWidth() * scale;
                float drawHeight = slice.getHeight() * scale;

                content.drawImage(pdImage, MARGIN, pageY - drawHeight, drawWidth, drawHeight);
            }

            y += sliceHeight;
        }
    }

    private byte[] toPngBytes(BufferedImage image) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}