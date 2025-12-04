package tmoney.co.kr.hxz.common.file.service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tmoney.co.kr.hxz.common.file.domain.FilesStorageProperties;
import tmoney.co.kr.hxz.common.file.mapper.FileMapper;
import tmoney.co.kr.hxz.common.file.vo.AttachmentVO;

@RequiredArgsConstructor
@Service
public class AttachmentService {

    private final FilesStorageProperties filesStorageProperties;
    private final FileMapper fileMapper;


    public AttachmentVO getAttachment(String orgCd, Long atflMngNo) {
        AttachmentVO vo = readAtflPath(orgCd, atflMngNo);
        if (vo == null) {
            throw new IllegalArgumentException("첨부파일 메타데이터를 찾을 수 없습니다.");
        }
        return vo;
    }

    @Transactional(readOnly = true)
    public AttachmentVO readAtflPath(String orgCd, Long atflMngNo) {
        return fileMapper.readAtflPath(orgCd, atflMngNo);
    }

    /**
     * 실제 파일 Resource 반환
     */
    public Resource loadAsResource(AttachmentVO attachment) {
        try {
            // baseDir + 상대 경로(atfl_path_nm) 조합
            String baseDir = filesStorageProperties.getBaseDir();
            String relativePath = attachment.getAtflPathNm();

            if (!StringUtils.hasText(baseDir) || !StringUtils.hasText(relativePath)) {
                throw new IllegalStateException("파일 경로 설정이 잘못되었습니다.");
            }

            Path root = Paths.get(baseDir).toAbsolutePath().normalize();
            Path file = root.resolve(relativePath).normalize();

            // 디렉터리 탈출 방지
            if (!file.startsWith(root)) {
                throw new SecurityException("잘못된 파일 경로 요청입니다.");
            }

            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                throw new IllegalArgumentException("실제 파일을 찾을 수 없습니다.");
            }

            return new UrlResource(file.toUri());
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("파일 경로가 올바르지 않습니다.", ex);
        }
    }
}