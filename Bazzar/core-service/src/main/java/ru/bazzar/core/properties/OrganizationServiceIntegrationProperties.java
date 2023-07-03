package ru.bazzar.core.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

import java.util.Objects;

@ConfigurationPropertiesBinding
@ConfigurationProperties(prefix = "integrations.organization-service")
@Getter
@Setter
@NoArgsConstructor
public class OrganizationServiceIntegrationProperties {
    private String url;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer writeTimeout;

    @Override
    public String toString() {
        return "OrganizationServiceIntegrationProperties{" +
                "url='" + url + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationServiceIntegrationProperties that = (OrganizationServiceIntegrationProperties) o;
        return Objects.equals(url, that.url) && Objects.equals(connectTimeout, that.connectTimeout) && Objects.equals(readTimeout, that.readTimeout) && Objects.equals(writeTimeout, that.writeTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, connectTimeout, readTimeout, writeTimeout);
    }
}
