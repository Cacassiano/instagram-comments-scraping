package dev.cacassiano.comment_scraper.services;

import dev.cacassiano.comment_scraper.models.InstagramComment;
import dev.cacassiano.comment_scraper.repositories.InstagramCommentRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExcelExportService {

    private final InstagramCommentRepository commentRepository;

    public ExcelExportService(InstagramCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Generate an XLSX workbook containing all Instagram comments from DB.
     * Columns: Nome de usuário, link perfil de usuário, comentario, numero de curtidas, Link da postagem
     */
    public byte[] generateCommentsExcel() throws Exception {
        List<InstagramComment> comments = commentRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Instagram Comments");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nome de usuário");
            header.createCell(1).setCellValue("Link perfil de usuário");
            header.createCell(2).setCellValue("Comentario");
            header.createCell(3).setCellValue("Numero de curtidas");
            header.createCell(4).setCellValue("Link da postagem");

            int rowIdx = 1;
            for (InstagramComment c : comments) {
                Row row = sheet.createRow(rowIdx++);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(c.getUsername() != null ? c.getUsername() : "");

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(c.getAuthorProfileUrl() != null ? c.getAuthorProfileUrl() : "");

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(c.getComment() != null ? c.getComment() : "");

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(c.getLikesCount() != null ? c.getLikesCount() : 0L);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(c.getPostUrl() != null ? c.getPostUrl() : "");
            }

            // Auto-size columns (optional)
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

}
