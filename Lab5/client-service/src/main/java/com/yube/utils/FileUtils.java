package com.yube.utils;

import com.yube.model.TxtFile;
import com.yube.model.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static ZipFile readZipFile(String name, byte[] content) throws IOException {
        ZipFile zipFile = new ZipFile(name);
        List<TxtFile> txtFiles = new ArrayList<>();
        InputStream is = new ByteArrayInputStream(content);
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            FilterInputStream fis = new FilterInputStream(zis) {
                @Override
                public void close() throws IOException {
                    zis.closeEntry();
                }
            };
            String fileName = zipEntry.getName();
            if (fileName.endsWith(".txt")) {
                String fileContent = IOUtils.toString(fis, StandardCharsets.UTF_8.name());
                TxtFile txtFile = new TxtFile(fileName);
                txtFile.setContent(fileContent);
                txtFiles.add(txtFile);
            }
        }
        zipFile.setTxtFiles(txtFiles);
        return zipFile;
    }

    public static ZipFile readZipFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isNotEmpty(fileName) && !fileName.endsWith(".zip")) {
            throw new IllegalArgumentException("Passed file is not a zip file");
        }
        byte[] fileContentBytes = file.getBytes();
        ZipFile zipFile = FileUtils.readZipFile(fileName, fileContentBytes);
        if (zipFile.getTxtFiles().isEmpty()) {
            throw new IllegalArgumentException("Zip file is empty");
        }
        return zipFile;
    }
}
