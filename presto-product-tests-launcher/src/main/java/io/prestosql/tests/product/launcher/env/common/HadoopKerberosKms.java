/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.tests.product.launcher.env.common;

import com.google.common.collect.ImmutableList;
import io.prestosql.tests.product.launcher.docker.DockerFiles;
import io.prestosql.tests.product.launcher.env.Environment;
import io.prestosql.tests.product.launcher.env.EnvironmentConfig;

import javax.inject.Inject;

import java.util.List;

import static io.prestosql.tests.product.launcher.env.EnvironmentContainers.COORDINATOR;
import static io.prestosql.tests.product.launcher.env.EnvironmentContainers.HADOOP;
import static io.prestosql.tests.product.launcher.env.EnvironmentContainers.TESTS;
import static io.prestosql.tests.product.launcher.env.common.Standard.CONTAINER_TEMPTO_PROFILE_CONFIG;
import static java.util.Objects.requireNonNull;
import static org.testcontainers.utility.MountableFile.forHostPath;

public class HadoopKerberosKms
        implements EnvironmentExtender
{
    private final DockerFiles.ResourceProvider configDir;

    private final HadoopKerberos hadoopKerberos;

    private final String hadoopImagesVersion;

    @Inject
    public HadoopKerberosKms(DockerFiles dockerFiles, EnvironmentConfig environmentConfig, HadoopKerberos hadoopKerberos)
    {
        this.configDir = dockerFiles.getDockerFilesHostDirectory("common/hadoop-kerberos-kms/");
        this.hadoopKerberos = requireNonNull(hadoopKerberos, "hadoopKerberos is null");
        requireNonNull(environmentConfig, "environmentOptions is null");
        hadoopImagesVersion = requireNonNull(environmentConfig, "environmentConfig is null").getHadoopImagesVersion();
    }

    @Override
    public void extendEnvironment(Environment.Builder builder)
    {
        // TODO (https://github.com/prestosql/presto/issues/1652) create images with HDP and KMS
        String dockerImageName = "prestodev/cdh5.15-hive-kerberized-kms:" + hadoopImagesVersion;

        builder.configureContainer(HADOOP, container -> {
            container.setDockerImageName(dockerImageName);
            container
                    .withCopyFileToContainer(forHostPath(configDir.getPath("kms-core-site.xml")), "/etc/hadoop-kms/conf/core-site.xml");
        });

        builder.configureContainer(COORDINATOR, container -> container.setDockerImageName(dockerImageName));

        builder.configureContainer(TESTS, container -> {
            container.setDockerImageName(dockerImageName);
            container.withCopyFileToContainer(forHostPath(configDir.getPath("tempto-configuration.yaml")), CONTAINER_TEMPTO_PROFILE_CONFIG);
        });
    }

    @Override
    public List<EnvironmentExtender> getDependencies()
    {
        return ImmutableList.of(hadoopKerberos);
    }
}
