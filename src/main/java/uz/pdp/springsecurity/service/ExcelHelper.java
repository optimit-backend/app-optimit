package uz.pdp.springsecurity.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.springsecurity.payload.ExportExcelDto;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String SHEET = "product";

    public static boolean hasExcelFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }
        return true;
    }

        public static List<ExportExcelDto> excelToTutorials(InputStream is) {

        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<ExportExcelDto> exportExcelDtoList = new ArrayList<ExportExcelDto>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row row = rows.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = row.iterator();

                ExportExcelDto exportExcelDto = new ExportExcelDto();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            exportExcelDto.setProductName(currentCell.getStringCellValue());
                            break;
                        case 1:
                            exportExcelDto.setBuyPrice(currentCell.getNumericCellValue());
                            break;
                        case 2:
                            exportExcelDto.setSalePrice(currentCell.getNumericCellValue());
                            break;
                        case 3:
                            exportExcelDto.setAmount(currentCell.getNumericCellValue());
                            break;
                        case 4:
                            exportExcelDto.setMinQuantity(currentCell.getNumericCellValue());
                            break;
                        case 5:
                            exportExcelDto.setExpiredDate(currentCell.getDateCellValue());
                            break;
                        case 6:
                            currentCell.setCellType(CellType.STRING);
                            exportExcelDto.setBarcode(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                exportExcelDtoList.add(exportExcelDto);
                workbook.close();
            }
            return exportExcelDtoList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
}