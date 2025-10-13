
package com.example.thymeleaf.file.service;

import com.example.thymeleaf.file.domain.Domain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Pattern SEGMENT_SAFE = Pattern.compile("^[A-Za-z0-9_-]+$");

    private final Path baseRoot;

    // 필요하면 기본값도 줄 수 있음: @Value("${file.storage.base-root:/data001/hxz}")
    public FileStorageService(@Value("${file.storage.base-root}") String baseRootProp) {
        if (!StringUtils.hasText(baseRootProp)) {
            throw new IllegalArgumentException("file.storage.base-root 설정이 없습니다.");
        }
        this.baseRoot = Paths.get(baseRootProp).toAbsolutePath().normalize();
    }


    /** 예: /data001/hxz/{orgCode}/{domain.pathSegment}/{yyyyMMdd}/{UUID}.{ext} */
    public StoredFile store(MultipartFile file, String orgCode, Domain domain) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "unknown";
        originalName = StringUtils.cleanPath(originalName);
        if (originalName.contains("..")) {
            throw new IllegalArgumentException("유효하지 않은 파일명: " + originalName);
        }

        String safeOrg = sanitizeOrgCode(orgCode);
        String ymd = LocalDate.now(KST).format(DAY);

        Path targetDir = baseRoot
                .resolve(safeOrg)
                .resolve(domain.pathSegment())
                .resolve(ymd)
                .normalize();

        ensureWithinBaseRoot(targetDir);
        try {
            Files.createDirectories(targetDir); // 있으면 통과, 없으면 생성
        } catch (Exception e) {
            throw new RuntimeException("저장 디렉터리 생성 실패: " + targetDir, e);
        }

        String ext = extractExt(originalName);
        String savedName = UUID.randomUUID() + ext;

        Path destination = targetDir.resolve(savedName).normalize();
        ensureWithinBaseRoot(destination);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패: " + originalName, e);
        }

        String relative = "/" + baseRoot.relativize(destination).toString().replace('\\', '/');

        StoredFile result = new StoredFile();
        result.setOriginalName(originalName);
        result.setSavedName(savedName);
        result.setBaseRoot(baseRoot.toString());
        result.setRelativePath(relative);      // 예) /ORG1/banner/20251013/UUID.png
        result.setAbsolutePath(destination.toString());
        result.setSize(file.getSize());
        return result;
    }

    /** 상대경로(베이스 기준)로 로드: /ORG1/banner/20251013/UUID.png */
    public Resource loadAsResource(String relativePath) {
        try {
            if (!StringUtils.hasText(relativePath)) {
                throw new IllegalArgumentException("relativePath가 비어 있습니다.");
            }
            String cleaned = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            Path file = baseRoot.resolve(cleaned).normalize();
            ensureWithinBaseRoot(file);

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("파일을 읽을 수 없습니다: " + relativePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("잘못된 파일 경로: " + relativePath, e);
        }
    }

    /** 오늘 날짜(서버 기준) 폴더의 파일명 나열 */
    public Stream<String> listTodayFilenames(String orgCode, Domain domain) {
        String safeOrg = sanitizeOrgCode(orgCode);
        String ymd = LocalDate.now(KST).format(DAY);
        Path dir = baseRoot.resolve(safeOrg).resolve(domain.pathSegment()).resolve(ymd).normalize();
        ensureWithinBaseRoot(dir);

        try {
            if (!Files.exists(dir)) return Stream.empty();
            return Files.list(dir)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString());
        } catch (Exception e) {
            throw new RuntimeException("파일 목록 조회 실패: " + dir, e);
        }
    }

    // ===== 내부 유틸 =====

    private static String extractExt(String originalName) {
        int idx = originalName.lastIndexOf('.');
        if (idx < 0 || idx == originalName.length() - 1) return "";
        String ext = originalName.substring(idx); // ".png"
        return (ext.length() <= 10) ? ext : "";
    }

    private static String sanitizeOrgCode(String seg) {
        if (!StringUtils.hasText(seg)) {
            throw new IllegalArgumentException("orgCode는 비어 있을 수 없습니다.");
        }
        if (!SEGMENT_SAFE.matcher(seg).matches()) {
            throw new IllegalArgumentException("orgCode에 허용되지 않는 문자가 포함되어 있습니다: " + seg);
        }
        return seg;
    }

    private void ensureWithinBaseRoot(Path path) {
        if (!path.normalize().startsWith(baseRoot)) {
            throw new SecurityException("저장 경로가 베이스 루트를 벗어납니다: " + path);
        }
    }

    // ===== 결과 DTO (POJO) =====
    public static class StoredFile {
        private String originalName;
        private String savedName;
        private String baseRoot;
        private String relativePath;
        private String absolutePath;
        private long size;

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        public String getSavedName() { return savedName; }
        public void setSavedName(String savedName) { this.savedName = savedName; }
        public String getBaseRoot() { return baseRoot; }
        public void setBaseRoot(String baseRoot) { this.baseRoot = baseRoot; }
        public String getRelativePath() { return relativePath; }
        public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
        public String getAbsolutePath() { return absolutePath; }
        public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
}
