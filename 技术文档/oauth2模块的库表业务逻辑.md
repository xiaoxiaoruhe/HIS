# OAuth2模块库表业务逻辑文档

## 1. 业务逻辑概览

### 权限控制模型
```
用户 (whale_users) → 用户角色关联 (whale_user_roles) → 角色 (whale_roles)
角色 (whale_roles) → 角色菜单关联 (whale_role_menus) → 菜单 (whale_menus)
```

## 2. 核心业务逻辑

### 2.1 认证流程（LoadUserDetailService）
```java
// 用户名登录 -> 验证密码 -> 加载角色 -> 加载扩展信息
1. 通过 username 查询 whale_users 表
2. 根据 userId 查询 whale_user_roles 关联表获取角色
3. 通过 userId 查询 adm_practitioner 表获取医务人员信息
4. 通过 userId 查询 whale_user_organization_units 获取组织单元信息
5. 返回 SecurityUser 对象供认证使用
```

### 2.2 授权流程（ResourcesService）
```java
// 启动时加载权限 -> 缓存到Redis -> 网关验证
1. 查询所有菜单的 link_url
2. 对每个 link_url 查询 whale_role_menus 表获取允许的角色
3. 将 {link_url: [role1, role2]} 映射关系存入 Redis
4. 网关通过 URL 查询 Redis 验证角色权限
```

## 3. 数据库表结构与业务逻辑

### 3.1 核心认证表

#### whale_users (平台用户表)
```sql
- id: 用户唯一标识
- username: 用户名（登录名）
- password: 密码（BCrypt加密）
```

**业务逻辑**：存储系统用户的基础信息，用于身份认证。

#### whale_roles (角色表)
```sql
- id: 角色唯一标识
- name: 角色名称
- code: 角色编码（代码中使用 getCode() 获取）
- description: 角色描述
```

**业务逻辑**：定义系统中的角色，用于权限控制。

#### whale_menus (菜单/接口权限表)
```sql
- id: 菜单唯一标识
- name: 菜单名称
- link_url: 链接地址（用于权限控制）
- path: 路由地址
```

**业务逻辑**：定义菜单项和接口路径，用于权限验证。

### 3.2 关联关系表

#### whale_user_roles (用户角色关联表)
```sql
- id: 主键
- user_id: 用户标识
- role_id: 角色标识
```

**业务逻辑**：关联用户与角色，实现用户-角色多对多关系。

#### whale_role_menus (角色菜单关联表)
```sql
- id: 主键
- role_id: 角色标识
- menu_id: 菜单标识
```

**业务逻辑**：定义角色可访问的菜单/接口，实现角色-菜单多对多关系。

### 3.3 医疗领域扩展表

#### adm_practitioner (医务人员主数据表)
```sql
- id: 主键
- name: 人员姓名
- user_id: 关联平台用户主键
- dr_profttl_code: 职称编码
- bus_no: 院内工号
- phone: 联系电话
- org_id: 默认登录科室
- is_deleted: 软删标记
```

**业务逻辑**：存储医务人员信息，与平台用户关联，丰富用户信息。

#### whale_user_organization_units (用户组织单元关联表)
```sql
- id: 主键
- user_id: 用户标识
- organization_unit_id: 组织单元标识
```

**业务逻辑**：建立用户与组织单元（如科室）的关联关系。

## 4. 业务流程详解

### 4.1 登录认证流程
1. 用户提交用户名密码
2. 通过 `whale_users.username` 查找用户
3. 验证用户是否存在及密码是否正确
4. 加载用户的角色列表（`whale_user_roles` → `whale_roles`）
5. 加载用户扩展信息（医务人员信息、组织单元）
6. 构建 SecurityUser 返回认证结果

### 4.2 权限验证流程
1. 系统启动时，`ResourcesServiceImpl.loadData()` 加载权限配置
2. 从 `whale_menus` 获取所有接口路径（`link_url`）
3. 对每个接口路径，从 `whale_role_menus` 获取可访问的角色列表
4. 将 `{接口路径: [角色列表]}` 映射存入 Redis 缓存
5. 网关拦截请求，根据 URL 从 Redis 查询需要的角色权限
6. 验证用户是否拥有对应角色

### 4.3 Token生成逻辑
```java
// TokenEnhancer 在JWT中添加额外信息
{
  "sub": "username",
  "id": "userId",                    // 用户ID
  "username": "userName",           // 用户名
  "practitionerId": "practId",      // 医务人员ID
  "practitionerName": "doctorName", // 医务人员姓名
  "titleCode": "title",             // 职称编码
  "organizationUnits": {}           // 组织单元信息
}
```

## 5. SQL查询逻辑

### 5.1 用户角色查询
```sql
SELECT r.id, r.name, r.code, r.description 
FROM whale_roles r 
WHERE r.id IN (
    SELECT role_id FROM whale_user_roles 
    WHERE user_id = #{userId}
)
```

### 5.2 接口权限查询
```sql
SELECT r.id, r.code 
FROM whale_roles r 
WHERE r.id IN (
    SELECT role_id 
    FROM whale_role_menus 
    WHERE menu_id = (
        SELECT id 
        FROM whale_menus 
        WHERE link_url = #{path}
    )
)
```

### 5.3 医务人员信息查询
```sql
SELECT * FROM adm_practitioner 
WHERE user_id = #{userId} AND is_deleted = 0
```

### 5.4 组织单元查询
```sql
SELECT organization_unit_id 
FROM whale_user_organization_units 
WHERE user_id = #{userId}
```

## 6. 业务逻辑规则

### 安全设计考虑
- 使用软删除 (`is_deleted`) 确保数据完整性
- 通过关联表 (`whale_user_roles`, `whale_role_menus`) 实现灵活的角色权限分配
- 菜单表的 `link_url` 字段作为权限控制的核心

### 业务逻辑规则
- 一个用户可以有多个角色
- 一个角色可以访问多个接口/菜单
- 一个接口/菜单可以被多个角色访问
- 医务人员信息与用户一对一关联（通过 `user_id`）
- 用户可属于多个组织单元（通过 `whale_user_organization_units`）

## 7. 数据流转关系图
```
whale_users (用户) 
    └── (一对多) whale_user_roles ── (多对一) whale_roles (角色)
    ├── (一对多) adm_practitioner (医务人员信息) 
    └── (一对多) whale_user_organization_units (组织单元)
    
whale_roles (角色)
    └── (一对多) whale_role_menus ── (多对一) whale_menus (菜单/接口)
```