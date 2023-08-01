package de.uniba.pi.applicationsearcher.serverlessinfocollector.yamls;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class YamlRead {

    final static Yaml yaml;

    static {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }
    public static Optional<Serverless> readServerlessYaml(String content, String appName) {
        content = content.replaceAll("!", "_");
        Serverless result = null;
        try (InputStream inputstream = new ByteArrayInputStream(content.getBytes())) {

            result = yaml.loadAs(inputstream, Serverless.class);
        } catch (IOException e) {
            System.err.printf("Could not create stream for app %s%n%s", appName, e.getMessage());
        } catch (ScannerException e) {
            System.err.printf("Could not scan for app %s%n%s", appName, e.getMessage());
        } catch (YAMLException e) {
            System.err.printf("Could not parse for app %s%n%s", appName, e.getMessage());
        }
        return Optional.ofNullable(result);
    }

}
