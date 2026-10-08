# JVM Memory Leak Demo

Spring Boot 3 + Java 17 + Maven 演示项目：模拟 Excel 上传解析，并将解析结果写入全局缓存。

## 环境要求

- JDK 17+
- Maven 3.8+

## 启动

```bash
mvn spring-boot:run
```

建议为排查练习限制堆内存（更容易观察 OOM / GC 异常）：

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms128m -Xmx256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof"
```

## 接口

### 上传并解析 Excel

```http
POST /upload/{userId}
Content-Type: multipart/form-data

file: <excel文件>
```

**示例（PowerShell）：**

```powershell
curl.exe -X POST "http://localhost:8080/upload/user-001" -F "file=@sample.xlsx"
```

任意 `.xlsx` 文件均可；若文件行数不足，服务会自动补充模拟数据至约 5000 行，并附加约 64KB 二进制载荷，单次解析结果体积较大。

**响应示例：**

```json
{
  "resultId": "uuid",
  "userId": "user-001",
  "fileName": "sample.xlsx",
  "totalRows": 5000,
  "estimatedSizeBytes": 1234567,
  "cacheKey": "user-001",
  "parsedAt": "2026-07-13T13:00:00",
  "message": "文件解析成功，结果已写入全局缓存"
}
```

### 缓存统计（辅助观测）

```http
GET /cache/stats
```

## 压测建议

连续多次上传以观察堆内存趋势，例如：

```powershell
1..100 | ForEach-Object {
  $uid = "user-{0:D3}" -f ($_ % 10)
  curl.exe -s -X POST "http://localhost:8080/upload/$uid" -F "file=@sample.xlsx" | Out-Null
  Write-Host "upload $_ done"
}
```

或使用脚本：

```powershell
.\scripts\stress-upload.ps1 -Count 100
```

## 生成测试用 Excel

```bash
mvn test -Dtest=SampleExcelGeneratorTest
```

会在项目根目录生成 `sample.xlsx`。

## JVM 排查工具参考

| 工具 | 用途 |
|------|------|
| `jps -l` | 查看 Java 进程 PID |
| `jstat -gcutil <pid> 1000` | 观察 GC 与堆使用率 |
| `jmap -heap <pid>` | 查看堆配置与各区占用 |
| `jmap -histo:live <pid>` | 存活对象直方图 |
| `jcmd <pid> GC.heap_dump heap.hprof` | 手动 dump 堆 |
| VisualVM / MAT | 分析 heap dump，查找 GC Root 引用链 |

## 配置项

`application.yml`：

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `app.parse.rows-per-upload` | 5000 | 每次解析最少行数 |
| `app.parse.payload-size-kb` | 64 | 附加二进制载荷大小 |

## 项目结构

```
src/main/java/com/demo/leak/
├── LeakDemoApplication.java
├── cache/ParseResultCache.java      # 全局缓存
├── audit/AuditTrailService.java     # 审计追踪
├── service/ExcelParseService.java   # Excel 解析
├── controller/UploadController.java
└── model/
```
