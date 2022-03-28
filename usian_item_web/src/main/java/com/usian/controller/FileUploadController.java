package com.usian.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.usian.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Loser
 * @date 2021年11月20日 15:50
 */
@RestController
@RequestMapping("/file")
public class FileUploadController {

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif");

    @Autowired
    private FastFileStorageClient storageClient;
    @Value("${IMAGE_PATH}")
    private String IMAGE_PATH;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            // 校验文件的类型
            String contentType = file.getContentType();
            if (!CONTENT_TYPES.contains(contentType)) {
                return Result.error("文件类型不合法");
            }
            // 校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return Result.error("文件内容不合法");
            }

            // 保存到服务器：InputStream inputStream, long fileSize, String fileExtName, Set<MataData> metaDataSet
            String originalFilename = file.getOriginalFilename();
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath path = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);

            // 生成url地址，返回
            return Result.ok(IMAGE_PATH + path.getFullPath());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}
