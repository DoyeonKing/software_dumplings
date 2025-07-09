package com.example.springboot.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable; // 推荐实现Serializable接口，尤其是在分布式或缓存场景下

@Schema(description = "用户实体信息") // 为整个User模型添加描述
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // 序列化ID

    @Schema(description = "用户唯一ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "用户邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    // --- 构造函数 ---

    // 无参构造函数：
    // Spring Framework (特别是Jackson库用于JSON序列化/反序列化) 需要一个无参构造函数来创建对象实例。
    public User() {
    }

    // 全参构造函数：
    // 方便在代码中快速创建User对象，如UserController中的示例：new User(1L, "Alice", "alice@example.com")
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // --- Getter 和 Setter 方法 ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // --- toString 方法 (可选，但推荐用于日志和调试) ---
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // --- equals 和 hashCode 方法 (可选，如果User对象需要作为Map的键或存储在Set中时推荐) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;
        if (!name.equals(user.name)) return false;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }
}
