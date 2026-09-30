package com.zeroone.cloud.plugin.gencode;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 描述：生成代码插件目标类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
@Mojo(name = "generate-code")
public class CodeGeneratorMojo extends AbstractMojo {
    /**
     * 数据库配置
     */
    @Parameter(required = true)
    private Map<String, String> dbConfig;

    /**
     * 输出目录
     */
    @Parameter(required = true)
    private String outDir;

    /**
     * 包名
     */
    @Parameter(defaultValue = "com.zeroone.star")
    private String pkgName;

    /**
     * mapper xml文件输出目录
     */
    @Parameter(defaultValue = "mapper")
    private String xmlDirName;

    /**
     * 过滤表前缀,多个用逗号分割
     */
    @Parameter(defaultValue = " ")
    private String tablePrefix;

    /**
     * 过滤表后缀,多个用逗号分割
     */
    @Parameter(defaultValue = " ")
    private String tableSuffix;

    /**
     * 创建时间字段
     */
    @Parameter(defaultValue = "create_time")
    private String createTimeColumn;

    /**
     * 创建人字段
     */
    @Parameter(defaultValue = "create_by")
    private String createByColumn;

    /**
     * 更新时间字段
     */
    @Parameter(defaultValue = "update_time")
    private String updateTimeColumn;

    /**
     * 更新人字段
     */
    @Parameter(defaultValue = "update_by")
    private String updateByColumn;

    /**
     * 逻辑删除字段
     */
    @Parameter(defaultValue = "deleted")
    private String logicDeleteColumn;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String defaultUrlArgs = "useUnicode=true&useSSL=false&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String urlArgs = dbConfig.get("args") == null ? defaultUrlArgs : dbConfig.get("args");
        String url = "jdbc:mysql://" + dbConfig.get("host") + ":" + dbConfig.get("port") + "/" + dbConfig.get("dbname") + "?" + urlArgs;
        FastAutoGenerator.create(url, dbConfig.get("username"), dbConfig.get("password"))
                // 全局配置
                .globalConfig((scanner, builder) -> builder
                        .author(scanner.apply("Please enter author name:"))
                        .disableOpenDir()   //禁止生成成功打开文件夹
                        .outputDir(outDir) //设置输出路径
                )
                // 包配置
                .packageConfig((scanner, builder) -> builder
                        .parent(pkgName)
                        .moduleName(scanner.apply("Please enter parent package name:"))
                        .pathInfo(Collections.singletonMap(OutputFile.xml, outDir + "\\..\\resources\\" + xmlDirName))
                )
                // 策略配置
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("Please enter the table names (separated by English \",\"). Enter \"all\" to select all tables:")))
                        .addTablePrefix(tablePrefix.split(","))
                        .addTableSuffix(tableSuffix.split(","))
                        .entityBuilder().enableLombok().enableFileOverride()
                        // 添加字段填充
                        .addTableFills(
                                new Column(createTimeColumn, FieldFill.INSERT),
                                new Column(createByColumn, FieldFill.INSERT),
                                new Column(updateTimeColumn, FieldFill.INSERT_UPDATE),
                                new Column(updateByColumn, FieldFill.INSERT_UPDATE))
                        // 逻辑删除字段
                        .logicDeleteColumnName(logicDeleteColumn)
                        .mapperBuilder().enableFileOverride()
                        .serviceBuilder()
                        .controllerBuilder().enableRestStyle().enableHyphenStyle()
                        .build()
                )
                .execute();
    }

    /**
     * 处理 all 情况
     * @param tables 表名字符串
     * @return 处理结果
     */
    private static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}
