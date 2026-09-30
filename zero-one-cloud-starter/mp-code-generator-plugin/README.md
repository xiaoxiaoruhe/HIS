# 工程简介

自定义mp代码生成插件，将代码生成过程固化下来，通过引入插件的方式完成。

使用插件配置参考：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.zeroone.star</groupId>
            <artifactId>mp-code-generator-plugin</artifactId>
            <version>2.0.0-SNAPSHOT</version>
            <configuration>
                <!-- 数据库连接信息配置 -->
                <dbConfig>
                    <!-- 数据库主机地址 -->
                    <host>192.168.220.128</host>
                    <!-- 数据库端口 -->
                    <port>3306</port>
                    <!-- 数据库名称 -->
                    <dbname>test</dbname>
                    <!-- 数据库用户名 -->
                    <username>root</username>
                    <!-- 数据库密码 -->
                    <password>123456</password>
                    <!-- 连接参数，可选参数默认值如下 -->
                    <args>useUnicode=true&amp;useSSL=false&amp;characterEncoding=utf-8&amp;serverTimezone=Asia/Shanghai&amp;allowPublicKeyRetrieval=true</args>
                </dbConfig>
                <!-- 生成代码输出根目录 -->
                <outDir>${project.basedir}/src/main/java</outDir>
                <!-- 源码用到的包名，可选参数默认值如下 -->
                <pkgName>com.zeroone.star</pkgName>
                <!-- mapper xml输出目录名称，可选参数默认值如下 -->
                <xmlDirName>mapper</xmlDirName>
                <!-- 过滤表前缀，可选参数示例值如下 -->
                <tablePrefix>t_,c_,sys_</tablePrefix>
                <!-- 过滤表后缀，可选参数示例值如下 -->
                <tableSuffix>_flag,_end</tableSuffix>
                <!-- 创建时间字段，可选参数默认值如下 -->
                <createTimeColumn>create_time</createTimeColumn>
                <!-- 创建人字段，可选参数默认值如下 -->
                <createByColumn>create_by</createByColumn>
                <!-- 更新时间字段，可选参数默认值如下 -->
                <updateTimeColumn>update_time</updateTimeColumn>
                <!-- 更新人字段，可选参数默认值如下 -->
                <updateByColumn>update_by</updateByColumn>
                <!-- 逻辑删除字段，可选参数默认值如下 -->
                <logicDeleteColumn>deleted</logicDeleteColumn>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-logging</artifactId>
                    <version>${spring-boot.version}</version>
                </dependency>
                <dependency>
                    <groupId>com.mysql</groupId>
                    <artifactId>mysql-connector-j</artifactId>
                    <version>${mysql.version}</version>
                </dependency>
                <dependency>
                    <groupId>com.baomidou</groupId>
                    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                    <version>${mybaits.plus.version}</version>
                </dependency>
                <dependency>
                    <groupId>com.baomidou</groupId>
                    <artifactId>mybatis-plus-generator</artifactId>
                    <version>${mybaits.plus.generator.version}</version>
                </dependency>
                <dependency>
                    <groupId>org.apache.velocity</groupId>
                    <artifactId>velocity-engine-core</artifactId>
                    <version>${apache.velocity.version}</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

