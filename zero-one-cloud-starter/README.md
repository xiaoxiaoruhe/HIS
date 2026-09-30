# 工程简介

用于封装自定义cloud-stater的父工程

## 目录结构说明

> `zero-one-cloud-starter`  
>
> > `push-codeup.cmd`：发布制品到仓库命令工具
> >
> > `fastdfs-spring-boot-starter` ：SpringBoot2.x的高性能FastDFS客户端
> >
> > `mp-code-generator-plugin`：自定义mp代码生成插件
> >
> > `zero-one-cloud-oauth2-entity` ：OAuth2-starter封装实体类类定义
> >
> > `zero-one-cloud-starter-oauth2`：对`OAuth2`认证服务进行二次starter封装
> >
> > `zero-one-cloud-starter-gateway` ： Gateway鉴权进行二次starter封装
> >
> > `assistant-agent`：它是一个**能理解、能行动、能学习**的智能助手解决方案，第三方子模块

## 关于子模块

使用参考：https://git-scm.com/docs/git-submodule/zh_HANS-CN

在 Git 仓库中引入其他仓库，最常用且官方推荐的方法是使用 **Git Submodule（子模块）**。它允许你在主项目中嵌套独立的外部仓库，同时各自保持独立的提交历史与分支管理。

### 快速操作

1. 引入其他仓库

在主仓库根目录下运行以下命令，将外部仓库作为子模块引入：

```bash
git submodule add <外部仓库URL> <本地目录路径>
```

*执行后，主仓库会生成一个 `.gitmodules` 文件，并记录当前子模块的特定提交版本。*

2. 克隆包含子模块的仓库

当其他人（或你在新电脑上）克隆该主仓库时，子模块文件夹默认是空的。需要运行以下命令来拉取子模块内容： 

```bash
# 初始化并拉取所有子模块数据
git submodule update --init --recursive
```

3. 更新子模块代码

进入子模块目录并拉取最新代码：

```bash
cd <本地目录路径>
git pull
```

*注意：子模块会停留在特定的提交记录上，当子模块有更新时，主仓库需要重新 `git add` 和 `git commit` 来锁定最新的子模块版本。*
