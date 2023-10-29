package com.lzb.shortvideo.controller;

import cn.hutool.core.io.FileUtil;
import com.lzb.shortvideo.common.BaseResponse;
import com.lzb.shortvideo.common.ErrorCode;
import com.lzb.shortvideo.common.ResultUtils;
import com.lzb.shortvideo.config.CosClientConfig;
import com.lzb.shortvideo.exception.BusinessException;
import com.lzb.shortvideo.manager.CosManager;
import com.lzb.shortvideo.model.dto.file.UploadFileRequest;
import com.lzb.shortvideo.model.entity.User;
import com.lzb.shortvideo.model.enums.FileUploadBizEnum;
import com.lzb.shortvideo.model.vo.UploadFileVo;
import com.lzb.shortvideo.service.UserService;
import com.qiniu.processing.OperationStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private static final List<String> PERMISSIBLE_USER_AVATAR_FORMATS =
            Arrays.asList("jpeg", "jpg", "svg", "png", "webp");

    private static final List<String> PERMISSIBLE_VIDEO_FORMATS =
            Arrays.asList("avi", "mp4", "mkv", "mov", "wmv", "flv", "rmvb", "mpeg", "3gp", "mpg");


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<UploadFileVo> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                                 UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
        String thumbnailPath = null;
        if (FileUploadBizEnum.VIDEO.equals(fileUploadBizEnum)) {
            thumbnailPath = String.format("%s/%s/%s.jpg", FileUploadBizEnum.THUMBNAIL, userId, filename);
        }
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            File finalFile = file;
            CompletableFuture.runAsync(() -> {
                cosManager.putObject(finalFile, userId, filename, fileUploadBizEnum);
            }, threadPoolExecutor);
            // 返回可访问地址
            String cosHost = cosClientConfig.getCosHost();
            UploadFileVo uploadFileVo = new UploadFileVo(cosHost, filepath, thumbnailPath);
            return ResultUtils.success(uploadFileVo);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 数据处理完成结果通知接受接口
     *
     * @return
     */
//    @PostMapping("/vframe")
    public void vframe(@RequestBody OperationStatus.OperationResult operationResult) {
        log.info("所执行的云处理操作命令状态码code={}", operationResult.code);
        log.info("所执行的云处理操作命令状态描述desc={}", operationResult.desc);
        log.info("如果处理失败，该字段会给出失败的详细原因error={}", operationResult.error);
        log.info("云处理结果保存在目标空间的文件名key={}", operationResult.key);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        final long ONE_M = 1024 * 1024L;
        final long ONE_G = 1024 * 1024 * 1024L;

        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 100m");
            }
            if (!PERMISSIBLE_USER_AVATAR_FORMATS.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else if (FileUploadBizEnum.VIDEO.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_G) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1G");
            }
            if (!PERMISSIBLE_VIDEO_FORMATS.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }

}
