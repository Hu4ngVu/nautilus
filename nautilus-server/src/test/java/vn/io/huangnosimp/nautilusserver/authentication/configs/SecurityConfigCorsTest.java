package vn.io.huangnosimp.nautilusserver.authentication.configs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigCorsTest {
    private CorsConfiguration corsFor(String domain) {
        var config = new SecurityConfig(null, null);
        ReflectionTestUtils.setField(config, "domain", domain);
        return config.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/users/alice"));
    }

    @Test
    void allowsClientOnItsOwnDomainWithCredentials() {
        var cors = corsFor("example.com");
        assertThat(cors.checkOrigin("https://example.com")).isEqualTo("https://example.com");
        assertThat(cors.checkOrigin("https://evil.example")).isNull();
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    void allowsEveryMethodTheClientSends() {
        var cors = corsFor("example.com");
        for (var method :
                new HttpMethod[] {
                    HttpMethod.GET,
                    HttpMethod.POST,
                    HttpMethod.PUT,
                    HttpMethod.PATCH,
                    HttpMethod.DELETE
                }) {
            assertThat(cors.checkHttpMethod(method)).as(method.name()).isNotNull();
        }
    }
}
