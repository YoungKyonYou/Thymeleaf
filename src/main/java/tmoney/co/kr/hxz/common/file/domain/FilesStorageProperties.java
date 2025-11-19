package tmoney.co.kr.hxz.common.file.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "file")
public class FilesStorageProperties {

    /**
     * 첨부파일 루트 디렉터리 (예: /data/attachments)
     */
    private String baseDir;

}
