package tmoney.co.kr.imports;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImportProviderRegistry {

    private final Map<String, ImportProvider<?>> providers;

    public ImportProviderRegistry(List<ImportProvider<?>> list) {
        this.providers = list.stream()
                .collect(Collectors.toUnmodifiableMap(ImportProvider::name, p -> p));
    }

    @SuppressWarnings("unchecked")
    public <T> ImportProvider<T> get(String name) {
        ImportProvider<?> p = providers.get(name);
        if (p == null) {
            throw new IllegalArgumentException("Unknown import provider: " + name);
        }
        return (ImportProvider<T>) p;
    }
}