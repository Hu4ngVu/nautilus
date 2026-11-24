package vn.io.huangnosimp.nautilusserver.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/** The upload Worker reports R2 object keys as resource ids, so keys must be exactly the id. */
class S3ObjectKeyConfigTest {
    @Test
    void presignedUrlsAddressObjectsByResourceIdInsideTheBucket() throws Exception {
        String pathStyle =
                PropertiesLoaderUtils.loadProperties(
                                new ClassPathResource("application.properties"))
                        .getProperty("spring.cloud.aws.s3.path-style-access-enabled");
        new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(
                                JacksonAutoConfiguration.class,
                                AwsAutoConfiguration.class,
                                CredentialsProviderAutoConfiguration.class,
                                RegionProviderAutoConfiguration.class,
                                S3AutoConfiguration.class))
                .withPropertyValues(
                        "spring.cloud.aws.credentials.access-key=test",
                        "spring.cloud.aws.credentials.secret-key=test",
                        "spring.cloud.aws.region.static=auto",
                        "spring.cloud.aws.s3.endpoint=https://account.r2.cloudflarestorage.com",
                        "spring.cloud.aws.s3.path-style-access-enabled=" + pathStyle)
                .run(
                        context -> {
                            var url =
                                    context.getBean(S3Template.class)
                                            .createSignedPutURL(
                                                    "nautilus",
                                                    "resource-id",
                                                    Duration.ofMinutes(1),
                                                    ObjectMetadata.builder()
                                                            .contentType("image/png")
                                                            .build(),
                                                    null);
                            assertEquals("account.r2.cloudflarestorage.com", url.getHost());
                            assertEquals("/nautilus/resource-id", url.getPath());
                        });
    }
}
