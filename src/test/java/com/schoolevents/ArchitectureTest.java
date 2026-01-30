package com.schoolevents;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(packages = "com.schoolevents", importOptions = { ImportOption.DoNotIncludeTests.class,
        ArchitectureTest.DoNotIncludeLauncher.class })
public class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonal_architecture_is_respected = onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..", "..domain.port.out..")
            .applicationServices("..application..")
            .adapter("persistence", "..adapter.out.persistence..")
            .adapter("email", "..adapter.out.email..")
            .adapter("ai", "..adapter.out.ai..")
            .adapter("filesystem", "..adapter.out.filesystem..")
            .adapter("cloud", "..adapter.out.cloud..")
            .withOptionalLayers(true);

    static class DoNotIncludeLauncher implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/launcher/") && !location.contains("\\launcher\\");
        }
    }
}
